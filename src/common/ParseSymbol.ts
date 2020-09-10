/**
 * Base class for all types of symbols
 */
export abstract class ParseSymbol {

    /** Should be unique for a given grammar */
    name: string;

    constructor(name: string) {
        this.name = name;
    }

    /* whether the symbol is terminal (i.e a lexer token) or not */
    abstract isTerminal(): boolean;

    /**
     * @returns the name
     */
    toString(): string {
        return this.name;
    }

    /**
     * The unique id of a symbol, should be unique for a given grammar.
     */
    getUid(): string {
        return this.name;
    }

    /*
     * Due the notion of 'Extended symbol', this method returns the 'normal' symbol
     * associated with the current symbol. the ExtendedSymbol class overrides this method.
     */
    asSimpleSymbol(): ParseSymbol {
        return this;
    }
};
