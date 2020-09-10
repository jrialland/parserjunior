import { Terminal } from '../common/Terminal';
import { Automaton, AutomatonBuilder, State } from './automaton/Automaton';
import { CharConstraint } from './CharConstraint';

/**
 * Simple token that represents a single character
 */
export class SingleChar extends Terminal {

    character: string;

    constructor(c: string) {
        super(c);
        this.character = c;
    }

    toString(): string {
        return "'" + this.name + "'";
    }

    get automaton(): Automaton {
        let builder = AutomatonBuilder.forTokenType(this);
        let initial = builder.initialState();
        let final = builder.newFinalState();
        builder.addTransition(initial, CharConstraint.eq(this.character), final);
        return builder.build();
    }
};
