package net.jr.parser.errors;

import net.jr.common.Symbol;
import net.jr.parser.Rule;

public class ShiftReduceConflictException extends RuntimeException {

    private Symbol symbol;

    private Rule rule;

    public ShiftReduceConflictException(Symbol symbol, Rule rule) {
        this.symbol = symbol;
        this.rule = rule;
    }

}
