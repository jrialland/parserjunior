
import {Terminal} from '../../common/Terminal';
import { CharConstraint } from '../CharConstraint';

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
        return source.addTransition(constraint);
    }

    failedState():State {
        return FailedState;
    }

    build():Automaton {
        return new Automaton(this._initial);
    }
}