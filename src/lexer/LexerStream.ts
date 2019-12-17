import { Token } from "../common/Token";
import { Terminal } from "../common/Terminal";
import { State, Transition } from "./automaton/Automaton";
import { LexicalError } from "./LexicalError";
import { Position } from "../common/Position";
import { Eof } from "../common/SpecialTerminal";
import { Reader } from "../common/Reader";

export class LexerStream implements Iterator<Token> {

    initial:State;

    activeStates:Set<State>;

    candidate:Token;

    position:Position;

    startPosition:Position;

    matched:string;

    tokenModifier:(t:Token)=>Token;

    buffer:Array<Token>;

    go:boolean;

    reader:Reader;

    ignored:Terminal[];

    constructor(reader:Reader, terminals:Terminal[], ignored:Terminal[]) {
        this.reader = reader;
        this.ignored = ignored;
        this.initial = new State;
        this.candidate = null;
        this.matched = ''; 
        this.tokenModifier = (t) => t;
        this.buffer = [];
        this.go = true;
        let automatons = terminals.map(t => t.automaton);
        for(let t of ignored) {
            automatons.push(t.automaton);
        }

        for(let a of automatons) {
            let s = a.initialState;
            if(s != null) {
                this.initial.fallbackTransition = s.fallbackTransition;
                s.outgoing.forEach(t => this.initial.outgoing.push(t));
                if(s.finalState) {
                    this.initial.terminal = s.terminal;
                }
            }
        }
        this.activeStates = new Set([this.initial]);
        this.reassignIds();
        this.startPosition = this.position = Position.start();
    }

    private reassignIds():void {
        let toVisit = [this.initial];
        let viewed:State[] = [this.initial];
        let counter = 0;
        while(toVisit.length > 0) {
            let current = toVisit.pop();
            for(let t of current.outgoing) {
                if(!viewed.includes(t.target)) {
                    toVisit.push(t.target);
                    viewed.push(t.target);
                } 
            }
            current.id = counter++;
        }
    }

    pushback(token:Token):void {
        this.buffer.unshift(token);
    }

    [Symbol.iterator]() {
        return this;
    }
    
    public next(): IteratorResult<Token> {
        if(!this.go && this.buffer.length == 0) {
            return  {
                done:true,
                value:null
            };
        }
        while(this.buffer.length == 0) {
            this.go = this.step();
        }
        return {
            done:false,
            value:this.buffer.shift()
        };
    }

    private emit(c:string):void {
        this.emitToken(this.candidate);
        this.candidate = null;
        this.matched = '';
        this.activeStates.clear();
        this.activeStates.add(this.initial);
        this.reader.unshift();
        this.startPosition = this.position;
    }

    private emitToken(token:Token):void {
        for(let i of this.ignored) {
            if(i == token.tokenType) {
                return;
            }
        }
        token = this.tokenModifier(token);
        this.buffer.push(token);
    }

    private step():boolean {
        let c = this.reader.read();
        let shouldpushback = false;

        if(c == null) {

            if(this.candidate == null) {
                if(this.activeStates.size == 1) {
                    let active = this.activeStates.values().next().value;
                    if(active.fallbackTransition != null && active.fallbackTransition.target.finalState) {
                        let s = active.fallbackTransition.target;
                        this.candidate = new Token(s.terminal, this.startPosition, this.matched);
                    }
                }
            }
            
            if(this.candidate == null) {
                let active = this.activeStates.values().next().value;
                if(active != this.initial) {
                    throw new LexicalError(c, this.position);
                }
            } else if(this.candidate.text.length > 0) {
                this.emit(c);
            }

            this.emitToken(new Token(Eof, this.position, ''));
            return false;
        }

        // check valid transitions, find the new states
        let newStates:Set<State> = new Set();
        
        for(let state of this.activeStates) {
            for(let tr of state.outgoing) {
                if(tr.constraint.matches(c)) {
                    newStates.add(tr.target);
                }
            }
        }
        
        // nothing matched -> see if there are any fallback transition
        if(newStates.size == 0) {
            for(let s of this.activeStates) {
                if(s.fallbackTransition != null) {
                    newStates.add(s.fallbackTransition.target);
                    shouldpushback = true;
                }
            }
        }

        //update matched test
        if(shouldpushback) {
            this.reader.unshift();
        } else {
            this.matched += c;
        }

        // there are no active transition
        if(newStates.size == 0) {
            if(this.candidate == null) {
                throw new LexicalError(c, this.position);
            } else {
                this.emit(c);
                return true;
            }
        }

        //find the potential candidate
        let candStates:Array<State>=[];
        for(let s of newStates) {
            if(s.finalState) {
                candStates.push(s);
            }
        }
        if(candStates.length > 0) {
            candStates.sort((a,b) => b.terminal.priority - a.terminal.priority);
            this.candidate = new Token(candStates[0].terminal, this.startPosition, this.matched);
        }

        //update position
        this.position = this.position.updated(c);
        this.activeStates = newStates;
        return true;
    }
};