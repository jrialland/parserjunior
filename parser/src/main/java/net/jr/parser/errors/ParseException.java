package net.jr.parser.errors;

import net.jr.lexer.Lexeme;
import net.jr.lexer.Token;

import java.util.Collection;

public class ParseException extends IllegalArgumentException {

    private Token unexpectedToken;

    private Collection<Lexeme> expected;

    public ParseException(Token unexpectedToken, Collection<Lexeme> expected) {
        super("Unexpected token '" + unexpectedToken.toString() + "'");
        this.unexpectedToken = unexpectedToken;
        this.expected = expected;
    }

    public Token getUnexpectedToken() {
        return unexpectedToken;
    }

    public Collection<Lexeme> getExpected() {
        return expected;
    }
}
