package net.jr.lexer.basiclexemes;

import net.jr.lexer.Lexemes;
import net.jr.lexer.automaton.DefaultAutomaton;
import net.jr.lexer.impl.CharConstraint;
import net.jr.lexer.impl.LexemeImpl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static net.jr.lexer.impl.CharConstraint.Builder.inList;

public class COctal extends LexemeImpl {

    public COctal() {
        DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(this);
        DefaultAutomaton.Builder.BuilderState init = builder.initialState();
        DefaultAutomaton.Builder.BuilderState got0 = builder.initialState();
        DefaultAutomaton.Builder.BuilderState finalState = builder.newFinalState();
        init.when(CharConstraint.Builder.eq('0')).goTo(got0);
        got0.when(CharConstraint.Builder.inList(Lexemes.OctalDigit)).goTo(finalState);
        finalState.when(CharConstraint.Builder.inList(Lexemes.OctalDigit)).goTo(finalState);
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
