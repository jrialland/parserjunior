package net.jr.lexer.basicterminals;

import net.jr.lexer.automaton.DefaultAutomaton;
import net.jr.lexer.impl.TerminalImpl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static net.jr.lexer.impl.CharConstraint.Builder.eq;
import static net.jr.lexer.impl.CharConstraint.Builder.or;

public class CBinary extends TerminalImpl {

    public CBinary() {
        setName("cBinary");
        DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(this);
        DefaultAutomaton.Builder.BuilderState init = builder.initialState();
        DefaultAutomaton.Builder.BuilderState got0 = builder.newNonFinalState();
        DefaultAutomaton.Builder.BuilderState gotB = builder.newNonFinalState();
        DefaultAutomaton.Builder.BuilderState finalState = builder.newFinalState();
        init.when(eq('0')).goTo(got0);
        got0.when(or(eq('B'), eq('b'))).goTo(gotB);
        gotB.when(or(eq('0'), eq('1'))).goTo(finalState);
        finalState.when(or(eq('0'), eq('1'))).goTo(finalState);
        setAutomaton(builder.build());
    }

    public static CBinary unMarshall(DataInputStream in) throws IOException {
        return TerminalImpl.unMarshall(new CBinary(), in);
    }

    @Override
    public String toString() {
        return "CBinary";
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

}
