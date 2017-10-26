package net.jr.parser;

import net.jr.lexer.Lexeme;

import java.io.StringWriter;
import java.util.Set;

public class ParseError extends RuntimeException {

    private Set<Lexeme> expected;

    private static String getDefaultMessage(Set<Lexeme> expected) {
        StringWriter sw = new StringWriter();
        sw.append("Parse errror");
        if(!expected.isEmpty()) {
            sw.append(" - ");
            if(expected.size()==1) {
                sw.append("expected : ");
                sw.append(expected.iterator().next().toString());
            } else {
                sw.append("expected one of ");
                sw.append(expected.toString());
            }
        }
        return sw.toString();
     }

    public ParseError(Set<Lexeme> expected) {
        super(getDefaultMessage(expected));
        this.expected = expected;
    }

    public Set<Lexeme> getExpected() {
        return expected;
    }
}
