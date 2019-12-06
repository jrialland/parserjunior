import {Terminal} from '../common/Terminal';
import {Automaton, AutomatonBuilder, State} from './automaton/Automaton';
import { CharConstraint } from './CharConstraint';

export class Literal extends Terminal {
    
    chars:string;

    constructor(chars:string) {
        super("Literal('" + chars + "')");
    }

    get automaton():Automaton {
        let builder = AutomatonBuilder.forTokenType(this);
        let currentState = builder.initialState();
        for(let i=0; i < this.chars.length; i+=1) {
            let c = this.chars[i];
            let targetState:State;
            if(i == this.chars.length -1) {
                targetState = builder.newFinalState();
            } else {
                targetState = builder.newNonFinalState();
            }
            builder.addTransition(currentState, CharConstraint.eq(c), targetState);
            currentState = targetState;
        }
        return builder.build();
    }

};
