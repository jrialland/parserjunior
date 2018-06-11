package net.jr.lexer.impl;

import net.jr.lexer.automaton.DefaultAutomaton;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Define a lexeme that can be any of the characters passed to its constructor.
 */
public class OneOf extends TerminalImpl {

    private String chars;

    public OneOf(final String chars) {
        this.chars = chars;
        DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(this);
        DefaultAutomaton.Builder.BuilderState initialState = builder.initialState();
        initialState.when(CharConstraint.Builder.inList(chars)).goTo(builder.newFinalState());
        setAutomaton(builder.build());
    }

    @SuppressWarnings("unused")
    public static OneOf unMarshall(DataInputStream in) throws IOException {
        String chars = in.readUTF();
        return new OneOf(chars);
    }

    @Override
    public String toString() {
        return String.format("OneOf('%s')", chars);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!obj.getClass().equals(OneOf.class)) {
            return false;
        }
        final OneOf o = (OneOf) obj;
        return chars.equals(o.chars);
    }

    @Override
    public int hashCode() {
        return -17 * chars.hashCode();
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public void marshall(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeUTF(chars);
    }
}
