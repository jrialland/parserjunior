package net.jr.lexer.impl;

import net.jr.lexer.Lexemes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class CFloatingPoint extends LexemeImpl {


    public CFloatingPoint() {
        DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(this);
        DefaultAutomaton.Builder.BuilderState initialState = builder.initialState();
        DefaultAutomaton.Builder.BuilderState beforeDot = builder.newNonFinalState();
        DefaultAutomaton.Builder.BuilderState finalState = builder.newFinalState();
        DefaultAutomaton.Builder.BuilderState gotSuffix = builder.newFinalState();
        DefaultAutomaton.Builder.BuilderState gotExponent;
        DefaultAutomaton.Builder.BuilderState nonFinalDot = builder.newNonFinalState();

        // [0-9]+ DOT [0-9]*
        initialState.when(CharConstraint.Builder.inList(Lexemes.Numbers)).goTo(beforeDot);
        beforeDot.when(CharConstraint.Builder.inList(Lexemes.Numbers)).goTo(beforeDot);
        beforeDot.when(CharConstraint.Builder.eq('.')).goTo(finalState);
        finalState.when(CharConstraint.Builder.inList(Lexemes.Numbers)).goTo(finalState);

        // DOT [0-9]+
        initialState.when(CharConstraint.Builder.eq('.')).goTo(nonFinalDot);
        nonFinalDot.when(CharConstraint.Builder.inList(Lexemes.Numbers)).goTo(finalState);

        gotExponent = addExponent(builder, finalState);
        gotExponent.when(CharConstraint.Builder.inList("lfLF")).goTo(builder.newFinalState());

        finalState.when(CharConstraint.Builder.inList("lfLF")).goTo(gotSuffix);
        addExponent(builder, gotSuffix);

        setAutomaton(builder.build());
    }

    private static DefaultAutomaton.Builder.BuilderState addExponent(DefaultAutomaton.Builder builder, DefaultAutomaton.Builder.BuilderState state) {
        DefaultAutomaton.Builder.BuilderState gotExp = builder.newNonFinalState();
        DefaultAutomaton.Builder.BuilderState gotSign = builder.newNonFinalState();
        DefaultAutomaton.Builder.BuilderState finalState = builder.newFinalState();
        state.when(CharConstraint.Builder.or(CharConstraint.Builder.eq('E'), CharConstraint.Builder.eq('e'))).goTo(gotExp);
        gotExp.when(CharConstraint.Builder.inList(Lexemes.NumbersExceptZero)).goTo(finalState);
        gotExp.when(CharConstraint.Builder.or(CharConstraint.Builder.eq('+'), CharConstraint.Builder.eq('-'))).goTo(gotSign);
        gotSign.when(CharConstraint.Builder.inList(Lexemes.NumbersExceptZero)).goTo(finalState);
        finalState.when(CharConstraint.Builder.inList(Lexemes.Numbers)).goTo(finalState);
        return finalState;
    }

    @Override
    public String toString() {
        return "CFloatingPoint";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!obj.getClass().equals(CFloatingPoint.class)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return 1747474;
    }

    @Override
    public void marshall(DataOutputStream dataOutputStream) throws IOException {

    }

    public static CFloatingPoint unMarshall(DataInputStream in) throws IOException {
        return new CFloatingPoint();
    }
}
