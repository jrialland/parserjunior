import { Terminal } from '../common/Terminal';
import {Automaton, AutomatonBuilder, State} from './automaton/Automaton';
import { CharConstraint } from './CharConstraint';

/**
 * Simple token that represents a single character
 */
export class SingleChar extends Terminal {
    
    character:string;

    constructor(c: string) {
        super(c);
        this.character = c;
    }

    toString(): string {
        return "'" + this.name + "'";
    }
    
    get automaton():Automaton {
        let initial:State = new State;
        let final:State = new State;
        final.terminal = this;
        initial.addTransition(CharConstraint.eq(this.character)).target = final;
        return new Automaton(initial);
    }
};
