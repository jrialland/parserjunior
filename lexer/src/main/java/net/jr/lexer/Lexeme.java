package net.jr.lexer;

import net.jr.common.Symbol;

public interface Lexeme extends Symbol {

    @Override
    default boolean isTerminal() {
        return true;
    }

    default int getPriority() {
        return 1;
    }
}
