import { Terminal } from './Terminal';
import { Automaton } from '../lexer/automaton/Automaton';

/**
 * This specialization of the NonTerminal class to make the distinction between 'real' tokens
 * and some interal tokens that have a special meaning
 */
export class SpecialTerminal extends Terminal {

    constructor(name: string) {
        super(name);
    }

    get automaton(): Automaton {
        throw new Error("Not implemented");
    }
};

//------------------------------------------------------------------------------
/**
 * The Terminal that represents the end of input
 */
export const Eof = new SpecialTerminal('ᵉᵒᶠ');

/**
 * The terminal that represents
 */
export const Empty = new SpecialTerminal('ɛ');