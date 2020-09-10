import { ParseSymbol } from './ParseSymbol';
import { Automaton } from '../lexer/automaton/Automaton';

export abstract class Terminal extends ParseSymbol {

    constructor(name: string) {
        super(name);
    }

    isTerminal() {
        return true;
    }

    abstract get automaton(): Automaton;
};