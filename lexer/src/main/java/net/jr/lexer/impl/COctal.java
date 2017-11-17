package net.jr.lexer.impl;

import net.jr.lexer.Lexemes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static net.jr.lexer.impl.CharConstraint.Builder.eq;
import static net.jr.lexer.impl.CharConstraint.Builder.inList;

public class COctal extends LexemeImpl {

    public COctal() {
        DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(this);
        DefaultAutomaton.Builder.BuilderState init = builder.initialState();
        DefaultAutomaton.Builder.BuilderState got0 = builder.initialState();
        DefaultAutomaton.Builder.BuilderState finalState = builder.newFinalState();
        init.when(eq('0')).goTo(got0);
        got0.when(inList(Lexemes.OctalDigit)).goTo(finalState);
        finalState.when(inList(Lexemes.OctalDigit)).goTo(finalState);
        CInteger.addIntegerSuffix(builder, finalState);
        setAutomaton(builder.build());
    }

    @Override
    public String toString() {
        return "COctal";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!obj.getClass().equals(COctal.class)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return 0x514145;
    }

    @Override
    public void marshall(DataOutputStream dataOutputStream) throws IOException {

    }

    public static COctal unMarshall(DataInputStream in) throws IOException {
        return new COctal();
    }
}
