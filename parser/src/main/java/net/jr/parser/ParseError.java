package net.jr.parser;

import net.jr.common.Symbol;
import net.jr.lexer.Token;

import java.io.StringWriter;
import java.util.Set;

/**
 * raised when something goes wrong with parsing 'a.k.a' Syntax Error
 */
public class ParseError extends RuntimeException {

    private static final long serialVersionUID = 9898978115L;

    private Token token;

    private Set<Symbol> expected;

    private static String getDefaultMessage(Token token, Set<Symbol> expected) {
        StringWriter sw = new StringWriter();
        sw.append("Parse error");
        sw.append(" : ");
        sw.append("(");
        sw.append(token.getPosition().toString());
        sw.append(")");
        if (!expected.isEmpty()) {
            sw.append(" : ");
            if (expected.size() == 1) {
                sw.append("expected : ");
                sw.append(expected.iterator().next().toString());
            } else {
                sw.append("expected one of ");
                sw.append(expected.toString());
            }

            String tokenType = token.getTokenType().toString();
            String txt = token.getText();


            sw.append(" ( got ");
            sw.append(tokenType);

            if (txt != null && !txt.isEmpty()) {
                txt = "'" + txt + "'";
                if (!txt.equals(tokenType)) {
                    sw.append(" ");
                    sw.append(txt);
                }
            }
            sw.append(" instead)");
        }
        return sw.toString();
    }

    /**
     * @param unexpectedToken
     * @param expected        The basiclexemes that were expected according to parser's current state
     */
    public ParseError(Token unexpectedToken, Set<Symbol> expected) {
        super(getDefaultMessage(unexpectedToken, expected));
        this.token = unexpectedToken;
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
     * What terminal symbols we were expecting to see
     *
     * @return
     */
    public Set<Symbol> getExpected() {
        return expected;
    }
}
