package net.jr.lexer.basiclexemes;

import net.jr.lexer.automaton.Automaton;
import net.jr.lexer.automaton.DefaultAutomaton;
import net.jr.lexer.impl.LexemeImpl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static net.jr.lexer.impl.CharConstraint.Builder.*;

public class QuotedString extends LexemeImpl {

    private Automaton automaton;

    private char starChar;

    private char endChar;

    private char escapeChar;

    private char[] forbiddenChars;

    public QuotedString(char startChar, char endChar, char escapeChar, char[] forbiddenChars) {
        this.starChar = startChar;
        this.endChar = endChar;
        this.escapeChar = escapeChar;
        this.forbiddenChars = forbiddenChars;
        DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(this);
        DefaultAutomaton.Builder.BuilderState inString = builder.newNonFinalState();
        DefaultAutomaton.Builder.BuilderState escaping = builder.newNonFinalState();
        builder.initialState().when(eq(startChar)).goTo(inString);
        inString.when(eq(escapeChar)).goTo(escaping);
        inString.when(inList(forbiddenChars)).goTo(builder.failedState());
        inString.when(not(eq(endChar))).goTo(inString);
        escaping.when(any()).goTo(inString);
        inString.when(eq(endChar)).goTo(builder.newFinalState());
        this.automaton = builder.build();
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public Automaton getAutomaton() {
        return automaton;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!QuotedString.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final QuotedString o = (QuotedString) obj;
        if (o.starChar != starChar) {
            return false;
        }
        if (o.endChar != o.endChar) {
            return false;
        }
        if (o.escapeChar != o.escapeChar) {
            return false;
        }
        return Arrays.equals(forbiddenChars, o.forbiddenChars);
    }

    @Override
    public int hashCode() {
        return starChar + endChar * 10 + escapeChar * 100 + (forbiddenChars == null ? 0 : new String(forbiddenChars).hashCode());
    }

    @Override
    public String toString() {
        return "QuotedString";
    }

    @Override
    public void marshall(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeChar(starChar);
        dataOutputStream.writeChar(endChar);
        dataOutputStream.writeChar(escapeChar);
        dataOutputStream.writeInt(forbiddenChars.length);
        for (int i = 0; i < forbiddenChars.length; i++) {
            dataOutputStream.writeChar(forbiddenChars[i]);
        }
    }

    @SuppressWarnings("unused")
    public static QuotedString unMarshall(DataInputStream in) throws IOException {
        char starChar = in.readChar();
        char endChar = in.readChar();
        char escapeChar = in.readChar();
        int len = in.readInt();
        char[] forbiddenChars = new char[len];
        for (int i = 0; i < forbiddenChars.length; i++) {
            forbiddenChars[i] = in.readChar();
        }
        return new QuotedString(starChar, endChar, escapeChar, forbiddenChars);
    }
}
