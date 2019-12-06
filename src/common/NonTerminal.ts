import { ParseSymbol } from './ParseSymbol';

/** Any symbol that is not a terminal (i.e any symbol that does not represent a token) */
export class NonTerminal extends ParseSymbol {
    
    constructor(name: string) {
        super(name)
    }

    isTerminal(): boolean {
        return false;
    }

};