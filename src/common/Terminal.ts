import { ParseSymbol } from './ParseSymbol';
import { Automaton } from '../lexer/automaton/Automaton';

export abstract class Terminal extends ParseSymbol {

    public priority:number = 0;

    constructor(name: string) {
        super(name);
    }

    isTerminal() {
        return true;
    }

    abstract get automaton(): Automaton;
};