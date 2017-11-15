package net.jr.lexer.impl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static net.jr.lexer.impl.CharConstraint.Builder.inList;

/**
 * Define a lexeme that can be any of the characters passed to its constructor.
 */
public class OneOf extends LexemeImpl {

    private String chars;

    public OneOf(final String chars) {
        this.chars = chars;
        DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(this);
        DefaultAutomaton.Builder.BuilderState initialState = builder.initialState();
        initialState.when(inList(chars)).goTo(builder.newFinalState());
        setAutomaton(builder.build());
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

    @SuppressWarnings("unused")
    public static OneOf unMarshall(DataInputStream in) throws IOException {
        String chars = in.readUTF();
        return new OneOf(chars);
    }
}
