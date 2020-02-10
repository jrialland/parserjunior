import {Terminal} from '../common/Terminal';
import {Automaton, AutomatonBuilder, State} from './automaton/Automaton';
import { CharConstraint } from './CharConstraint';

/**
 * A Literal is a fixed sequence of characters (i.e a keyword)
 */
export class Literal extends Terminal {
    
    chars:string;

    constructor(chars:string) {
        super("Literal('" + chars + "')");
        this.chars = chars;
    }

    get automaton():Automaton {
        let builder = AutomatonBuilder.forTokenType(this);
        let currentState = builder.initialState();
        for(let i=0; i < this.chars.length; i+=1) {
            let c = this.chars[i];
            let nextState:State = i == this.chars.length-1 ? builder.newFinalState() : builder.newNonFinalState();
            let transition = currentState.addTransition(CharConstraint.eq(c));
            transition.target = nextState;
            currentState = nextState;
        }
        return builder.build();
    }

};
