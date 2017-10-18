package net.jr.lexer;

import net.jr.common.Symbol;

public interface Lexeme extends Symbol {

    Lexeme Eof = new Lexeme() {

        @Override
        public String toString() {
            return "Eof";
        }
    };

    @Override
    default boolean isTerminal() {
        return true;
    }
}
