package net.jr.lexer;

import net.jr.common.Symbol;

/**
 * A {@link Lexeme} is the basic unit of meaning in a grammar. It is a synonymn of "terminal symbol"
 */
public interface Lexeme extends Symbol {

    @Override
    default boolean isTerminal() {
        return true;
    }

    default int getPriority() {
        return 1;
    }
}
