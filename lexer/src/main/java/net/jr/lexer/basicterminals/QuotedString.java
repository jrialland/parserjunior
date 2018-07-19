package net.jr.lexer.basicterminals;

import net.jr.lexer.automaton.Automaton;
import net.jr.lexer.automaton.DefaultAutomaton;
import net.jr.lexer.impl.TerminalImpl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static net.jr.lexer.impl.CharConstraint.Builder.*;

public class QuotedString extends TerminalImpl {

    private Automaton automaton;

    private char starChar;

    private char endChar;

    private char escapeChar;

    private char[] forbiddenChars;

    public QuotedString(char startChar, char endChar, char escapeChar, char[] forbiddenChars) {
        init(startChar, endChar, escapeChar, forbiddenChars);
        setPriority(1);
    }

    private QuotedString() {

    }

    private void init(char startChar, char endChar, char escapeChar, char[] forbiddenChars) {
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

    @SuppressWarnings("unused")
    public static QuotedString unMarshall(DataInputStream in) throws IOException {
        QuotedString q = TerminalImpl.unMarshall(new QuotedString(), in);
        char starChar = in.readChar();
        char endChar = in.readChar();
        char escapeChar = in.readChar();
        int len = in.readInt();
        char[] forbiddenChars = new char[len];
        for (int i = 0; i < forbiddenChars.length; i++) {
            forbiddenChars[i] = in.readChar();
        }
        q.init(starChar, endChar, escapeChar, forbiddenChars);
        return q;
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
        super.marshall(dataOutputStream);
        dataOutputStream.writeChar(starChar);
        dataOutputStream.writeChar(endChar);
        dataOutputStream.writeChar(escapeChar);
        dataOutputStream.writeInt(forbiddenChars.length);
        for (int i = 0; i < forbiddenChars.length; i++) {
            dataOutputStream.writeChar(forbiddenChars[i]);
        }
    }
}
