package net.jr.parser;

import net.jr.lexer.Lexeme;
import net.jr.lexer.Token;

import java.io.StringWriter;
import java.util.Set;

public class ParseError extends RuntimeException {

    private Token token;

    private Set<Lexeme> expected;

    private static String getDefaultMessage(Token token, Set<Lexeme> expected) {
        StringWriter sw = new StringWriter();
        sw.append("Parse errror");
        if (!expected.isEmpty()) {
            sw.append(" - ");
            if (expected.size() == 1) {
                sw.append("expected : ");
                sw.append(expected.iterator().next().toString());
            } else {
                sw.append("expected one of ");
                sw.append(expected.toString());
            }
            sw.append(" ( got ");
            sw.append(token.getTokenType().toString());
            sw.append(" instead)");
        }
        return sw.toString();
    }

    public ParseError(Token token, Set<Lexeme> expected) {
        super(getDefaultMessage(token, expected));
        this.token = token;
        this.expected = expected;
    }

    /**
     * The erroneous token
     *
     * @return
     */
    public Token getToken() {
        return token;
    }

    /**
     * What we were expecting to see
     *
     * @return
     */
    public Set<Lexeme> getExpected() {
        return expected;
    }
}
