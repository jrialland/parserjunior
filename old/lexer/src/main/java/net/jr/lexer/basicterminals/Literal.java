package net.jr.lexer.basicterminals;

import net.jr.lexer.automaton.Automaton;
import net.jr.lexer.automaton.DefaultAutomaton;
import net.jr.lexer.impl.TerminalImpl;

import java.io.DataOutput;
import java.io.IOException;

import static net.jr.lexer.impl.CharConstraint.Builder.eq;

/**
 * A literal is a 'keyword' i.e a fixed string.
 */
public class Literal extends TerminalImpl {

    private String value;

    public Literal(String value) {
        this();
        this.value = value;
    }

    private Literal() {
        setPriority(2);
    }

    public static Literal unMarshall(java.io.DataInput in) throws IOException {
        Literal l = TerminalImpl.unMarshall(new Literal(), in);
        l.value = in.readUTF();
        return l;
    }

    @Override
    public Automaton getAutomaton() {
        DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(this);
        DefaultAutomaton.Builder.BuilderState currentState = builder.initialState();
        char[] chars = value.toCharArray();
        for (int i = 0, max = chars.length; i < max; i++) {
            char c = chars[i];
            final DefaultAutomaton.Builder.BuilderState targetState;
            if (i == max - 1) {
                targetState = builder.newFinalState();
            } else {
                targetState = builder.newNonFinalState();
            }
            currentState.when(eq(c)).goTo(targetState);
            currentState = targetState;
        }
        return builder.build();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!obj.getClass().equals(Literal.class)) {
            return false;
        }
        final Literal o = (Literal) obj;
        return value.equals(o.value);
    }

    @Override
    public int hashCode() {
        return 23 + value.hashCode();
    }

    @Override
    public String toString() {
        String name = getName();
        return name == null ? "'" + value + "'" : name;
    }

    @Override
    public void marshall(DataOutput dataOutputStream) throws IOException {
        super.marshall(dataOutputStream);
        dataOutputStream.writeUTF(value);
    }
}
