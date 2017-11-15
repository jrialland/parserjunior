package net.jr.lexer.impl;

import net.jr.lexer.Lexemes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static net.jr.lexer.impl.CharConstraint.Builder.*;

public class CCharacter extends LexemeImpl {

    public CCharacter() {
        DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(this);
        DefaultAutomaton.Builder.BuilderState init = builder.initialState();
        DefaultAutomaton.Builder.BuilderState gotFirstQuote = builder.newNonFinalState();
        DefaultAutomaton.Builder.BuilderState escaped = builder.newNonFinalState();
        DefaultAutomaton.Builder.BuilderState octalEscape = builder.newNonFinalState();
        DefaultAutomaton.Builder.BuilderState hexEscape = builder.newNonFinalState();
        DefaultAutomaton.Builder.BuilderState octalEscape2 = builder.newNonFinalState();
        DefaultAutomaton.Builder.BuilderState gotChar = builder.newNonFinalState();
        DefaultAutomaton.Builder.BuilderState universalEscape = builder.newNonFinalState();
        DefaultAutomaton.Builder.BuilderState gotHexQuad;
        DefaultAutomaton.Builder.BuilderState gotHexQuad2;
        DefaultAutomaton.Builder.BuilderState done = builder.newFinalState();

        init.when(eq('\'')).goTo(gotFirstQuote);
        gotFirstQuote.when(eq('\\')).goTo(escaped);
        gotFirstQuote.when(and(inRange(0x20, 128), not(eq('\\')))).goTo(gotChar);
        escaped.when(inList("\"?abfnrtv\\")).goTo(gotChar);

        escaped.when(inList(Lexemes.OctalDigit)).goTo(octalEscape);
        octalEscape.when(inList(Lexemes.OctalDigit)).goTo(octalEscape2);
        octalEscape.when(eq('\'')).goTo(done);
        octalEscape2.when(inList(Lexemes.OctalDigit)).goTo(gotChar);
        octalEscape2.when(eq('\'')).goTo(done);

        escaped.when(eq('x')).goTo(hexEscape);
        escaped.when(inList(Lexemes.HexDigit)).goTo(hexEscape);
        hexEscape.when(eq('\'')).goTo(done);

        escaped.when(or(eq('u'), eq('U'))).goTo(universalEscape);
        gotHexQuad = addHexQuad(builder, universalEscape);
        gotHexQuad.when(eq('\'')).goTo(done);
        gotHexQuad2 = addHexQuad(builder, gotHexQuad);
        gotHexQuad2.when(eq('\'')).goTo(done);

        gotChar.when(eq('\'')).goTo(done);
        setAutomaton(builder.build());
    }

    private static DefaultAutomaton.Builder.BuilderState addHexQuad(DefaultAutomaton.Builder builder, DefaultAutomaton.Builder.BuilderState origin) {
        DefaultAutomaton.Builder.BuilderState current = origin;
        for (int i = 0; i < 4; i++) {
            DefaultAutomaton.Builder.BuilderState from = current;
            current = builder.newNonFinalState();
            from.when(CharConstraint.Builder.inList(Lexemes.HexDigit)).goTo(current);
        }
        return current;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!obj.getClass().equals(CCharacter.class)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return -14814475;
    }

    @Override
    public void marshall(DataOutputStream dataOutputStream) throws IOException {

    }

    public static CCharacter unMarshall(DataInputStream in) throws IOException {
        return new CCharacter();
    }
}
