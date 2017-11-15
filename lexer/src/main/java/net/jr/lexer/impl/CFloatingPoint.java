package net.jr.lexer.impl;

import net.jr.lexer.Lexemes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static net.jr.lexer.impl.CharConstraint.Builder.*;

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
        initialState.when(inList(Lexemes.Numbers)).goTo(beforeDot);
        beforeDot.when(inList(Lexemes.Numbers)).goTo(beforeDot);
        beforeDot.when(eq('.')).goTo(finalState);
        finalState.when(inList(Lexemes.Numbers)).goTo(finalState);

        // DOT [0-9]+
        initialState.when(eq('.')).goTo(nonFinalDot);
        nonFinalDot.when(inList(Lexemes.Numbers)).goTo(finalState);

        gotExponent = addExponent(builder, finalState);
        gotExponent.when(inList("lfLF")).goTo(builder.newFinalState());

        finalState.when(inList("lfLF")).goTo(gotSuffix);
        addExponent(builder, gotSuffix);

        setAutomaton(builder.build());
    }

    private static DefaultAutomaton.Builder.BuilderState addExponent(DefaultAutomaton.Builder builder, DefaultAutomaton.Builder.BuilderState state) {
        DefaultAutomaton.Builder.BuilderState gotExp = builder.newNonFinalState();
        DefaultAutomaton.Builder.BuilderState gotSign = builder.newNonFinalState();
        DefaultAutomaton.Builder.BuilderState finalState = builder.newFinalState();
        state.when(or(eq('E'), eq('e'))).goTo(gotExp);
        gotExp.when(inList(Lexemes.NumbersExceptZero)).goTo(finalState);
        gotExp.when(or(eq('+'), eq('-'))).goTo(gotSign);
        gotSign.when(inList(Lexemes.NumbersExceptZero)).goTo(finalState);
        finalState.when(inList(Lexemes.Numbers)).goTo(finalState);
        return finalState;
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
