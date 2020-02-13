
import {Terminal} from '../../common/Terminal';
import { CharConstraint } from '../CharConstraint';
import jsesc = require('jsesc');

export class State {
    
    incoming:Array<Transition> = [];

	outgoing:Array<Transition> = [];
    
    fallbackTransition:Transition = null;
    
    terminal:Terminal = null;
    
    id:number = 0;

	addTransition(constraint:CharConstraint):Transition {
        let t = new Transition();
        t.constraint = constraint;
        t.source = this;
        this.outgoing.push(t);
        return t;
    }
    
	addFallback():Transition {
        this.fallbackTransition = new Transition();
        this.fallbackTransition.source = this;
        this.fallbackTransition.constraint = CharConstraint.any();
        return this.fallbackTransition;
    }
    
	disconnect() {
		for(let removed of this.outgoing) {
            removed.target.incoming = removed.target.incoming.filter(t=>t!=removed);
        }
        for(let removed of this.incoming) {
            removed.source.outgoing = removed.source.outgoing.filter(t=>t!=removed);
        }
    }
    
    get finalState():boolean {
        return this.terminal != null;
    }
};

export const FailedState = new State;

export class Transition {
	source:State;
	constraint:CharConstraint;
    target:State;
}

export class Automaton {
    
    private _initial:State;

    constructor(initialState:State) {
        this._initial = initialState;
    }

    get initialState():State {
        return this._initial;
    }

    *allTransitions() {
        let stack = [this._initial];
        let done:Array<State> = [];
        while(stack.length > 0) {
            let currentState = stack.pop();
            if(currentState.fallbackTransition && !done.includes(currentState.fallbackTransition.target)) {
                stack.push(currentState.fallbackTransition.target);
            }
            if(!done.includes(currentState)) {
                yield currentState;
                done.push(currentState);
                for(let t of currentState.outgoing) {
                    if(t.target && !stack.includes(t.target) && !done.includes(t.target)) {
                        stack.push(t.target)
                    }
                }
            }
        } 
    }

    toGraphviz():string {
        let s:string = `digraph ${this.constructor.name} {\n`; 
        for(let state of this.allTransitions()) {
            s += `    "${state.id}" `;
            if(state.id == 0) {
                s += "[shape=circle,penwidth=4]";
            } else if(state.finalState) {
                s += "[shape=doublecircle]";
            } else {
                s += "[shape=circle]";
            }
            s+=";\n";
        }
        let stack = [this.initialState];
        let done:Array<State> = [];
        while(stack.length) {
            let state = stack.pop();
            done.push(state);
            for(let transition of state.outgoing) {
                let label = jsesc(transition.constraint.toString(),{quotes:'double'});
                s += `    "${state.id}" -> "${transition.target.id}" [label="${label}"];\n`;
                if(!done.includes(transition.target)) {
                    stack.push(transition.target);
                }
            }
            if(state.fallbackTransition) {
                s += `    "${state.id}" -> "${state.fallbackTransition.target.id}" [style=dashed];\n`;
                if(!done.includes(state.fallbackTransition.target)) {
                    stack.push(state.fallbackTransition.target);
                }
            }
        }
        s+="}"
        return s;
    }
}

export class AutomatonBuilder {

    private _terminal:Terminal;

    private _initial:State;

    static forTokenType(t:Terminal):AutomatonBuilder {
        let builder:AutomatonBuilder = new AutomatonBuilder();
        builder._terminal = t;
        builder._initial = new State();
        return builder;
    }

    initialState():State {
        return this._initial;
    }

    newFinalState():State {
        let s = new State();
        s.terminal = this._terminal;
        return s;
    }

    newNonFinalState():State {
        let s = new State();
        s.terminal = null
        return s;
    }

    addTransition(source:State, constraint:CharConstraint, target:State):Transition {
        let t = source.addTransition(constraint);
        t.target = target;
        return t;
    }

    failedState():State {
        return FailedState;
    }

    build():Automaton {
        let a = new Automaton(this._initial);
        let counter = 0;
        for(let state of a.allTransitions()) {
            state.id = counter++;
        }
        return a;
    }
}