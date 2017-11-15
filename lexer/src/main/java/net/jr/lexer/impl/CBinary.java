package net.jr.lexer.impl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static net.jr.lexer.impl.CharConstraint.Builder.eq;
import static net.jr.lexer.impl.CharConstraint.Builder.or;

public class CBinary extends LexemeImpl {

    public CBinary() {
        DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(this);
        DefaultAutomaton.Builder.BuilderState init = builder.initialState();
        DefaultAutomaton.Builder.BuilderState got0 = builder.initialState();
        DefaultAutomaton.Builder.BuilderState gotB = builder.initialState();
        DefaultAutomaton.Builder.BuilderState finalState = builder.newFinalState();
        init.when(eq('0')).goTo(got0);
        got0.when(or(eq('B'), eq('b'))).goTo(gotB);
        gotB.when(or(eq('0'), eq('1'))).goTo(finalState);
        finalState.when(or(eq('0'), eq('1'))).goTo(finalState);
        setAutomaton(builder.build());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!obj.getClass().equals(CBinary.class)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return 1686118;
    }

    @Override
    public void marshall(DataOutputStream dataOutputStream) throws IOException {

    }

    public static CBinary unMarshall(DataInputStream in) throws IOException {
        return new CBinary();
    }
}
