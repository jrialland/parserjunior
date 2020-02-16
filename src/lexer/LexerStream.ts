import { Token } from "../common/Token";
import { Terminal } from "../common/Terminal";
import { State, Transition } from "./automaton/Automaton";
import { LexicalError } from "./LexicalError";
import { Position } from "../common/Position";
import { Eof } from "../common/SpecialTerminal";
import { Reader } from "../common/Reader";

export class LexerStream implements Iterator<Token> {

    /** initial state of the internal automaton */
    private initial:State;

    /* states that are currently active */
    private activeStates:Set<State>;

    /* the token type that is valid */
    private candidate:Token;

    /* current position in reader */
    private position:Position;

    /* initial position where the current token started matching */
    private startPosition:Position;

    /* shall we go on ? */
    private go:boolean;

    /* the data that is beeing read */
    private reader:Reader;

    /** the token types that we recognize but dont emit */
    private ignored:Terminal[];

    /** The text that is being matched */
    private matched:string;

    private buffer:Array<Token> = [];

    private terminals:Array<Terminal>;
    /** function that can be used to override token types on the fly (i.e useful for the C parser typedef 'hack') */
    tokenModifier:(t:Token)=>Token;

    constructor(reader:Reader, terminals:Terminal[], ignored:Terminal[]) {
        this.reader = reader;
        this.terminals = [].concat(terminals).concat(ignored);
        this.ignored = ignored;
        this.initial = new State;
        this.candidate = null;
        this.matched = ''; 
        this.tokenModifier = (t) => t;
        this.go = true;

        // make a big automaton for all recognized tokens
        let automatons = terminals.map(t => t.automaton);
        for(let t of ignored) {
            automatons.push(t.automaton);
        }
        for(let a of automatons) {
            let s = a.initialState;
            if(s != null) {
                s.outgoing.forEach(t => this.initial.outgoing.push(t));
                if(s.finalState) {
                    this.initial.terminal = s.terminal;
                }
            }
        }

        this.activeStates = new Set([this.initial]);
        this.startPosition = this.position = Position.start();
    }

    pushback(token:Token):void {
        this.buffer.unshift(token);
    }

    [Symbol.iterator]() {
        return this;
    }
    
    public next(): IteratorResult<Token> {
        
        // If we are done
        if(!this.go && this.buffer.length == 0) {
            return  {
                done:true,
                value:null
            };
        }
        // While the internal buffer is empty, scan the input
        while(this.buffer.length == 0) {
            this.go = this.step();
        }

        return {
            done:false,
            value:this.buffer.shift()
        };
    }


    private emitToken(token:Token):void {
        for(let i of this.ignored) {
            if(i == token.tokenType) {
                return;
            }
        }
        this.buffer.push(this.tokenModifier(token));
    }

    private emit() {
        let token = this.emitToken(this.candidate);
        this.activeStates.clear();
        this.activeStates.add(this.initial);
        this.candidate = null;
        this.matched = '';
        this.startPosition = this.position;
        this.reader.unshift();
    }

    /**
     * consume next char
     * @return false if there are more characters to read
     */
    private step():boolean {
        
        let c = this.reader.read();
        
        if(c == null) {
            // eof has been reached

            if( ! this.candidate) {
                //no candidate ? verify that there is note
                if(this.activeStates.size > 0) {
                    //having one active state is ok if it is the initial state, having more that one active state is not ok
                    if(this.activeStates.size > 1 || this.activeStates.entries().next().value != this.initial) {
                        throw new LexicalError("Unterminated token", this.position);
                    }
                } 
            } else if(this.candidate.text.length) {
                this.emitToken(this.candidate);
            }

            //emit eof token
            this.emitToken(new Token(Eof, this.position, ''));

            // no more steps
            return false;

        } else {

            // activate all transitions, updating current states
            let newStates:Set<State> = new Set;
            this.activeStates.forEach(s=> {
                for(let matching of s.outgoing.filter(t=>t.constraint.matches(c)).map(t=>t.target)) {
                    newStates.add(matching);
                }
            });
            this.activeStates = newStates;
            
            // Update position & matched text
            this.matched += c;
            
            //Find a new candidate
            let candidateTerminals = Array.from(newStates)
                .filter(s=>s.terminal!=null)
                .map(s=>s.terminal)
                .sort((a,b)=>this.terminals.indexOf(a) - this.terminals.indexOf(b));

            if(this.candidate) {
                if(candidateTerminals.indexOf(this.candidate.tokenType) == -1) {
                    this.emit();
                    return true;
                }
            }
            if(candidateTerminals.length) {
                this.candidate = new Token(candidateTerminals[0], this.startPosition, this.matched);
            }
            this.position = this.position.updated(c);
            return true;
        }
    }
};