
import {Terminal} from '../../common/Terminal';
import { CharConstraint } from '../CharConstraint';
import jsesc = require('jsesc');

export class State {
    
    incoming:Array<Transition> = [];

	outgoing:Array<Transition> = [];
        
    terminal:Terminal = null;
    
    id:number = 0;

	addTransition(constraint:CharConstraint):Transition {
        let t = new Transition();
        t.constraint = constraint;
        t.source = this;
        this.outgoing.push(t);
        return t;
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
        let counter = 0;
        for(let state of this.allTransitions()) {
            state.id = counter++;
        }
    }

    get initialState():State {
        return this._initial;
    }

    *allTransitions() {
        let stack = [this._initial];
        let done:Array<State> = [];
        while(stack.length > 0) {
            let currentState = stack.pop();
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

    makeFinal(state:State):State {
        state.terminal = this._terminal;
        return state;
    }

    addTransition(source:State, constraint:CharConstraint, target:State):Transition {
        let t = source.addTransition(constraint);
        t.target = target;
        target.incoming.push(t);
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