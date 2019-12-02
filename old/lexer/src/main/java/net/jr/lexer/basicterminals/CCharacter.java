package net.jr.lexer.basicterminals;

import net.jr.lexer.Lexemes;
import net.jr.lexer.automaton.DefaultAutomaton;
import net.jr.lexer.impl.CharConstraint;
import net.jr.lexer.impl.TerminalImpl;

import java.io.IOException;

public class CCharacter extends TerminalImpl {

    public CCharacter() {
        setName("cCharacter");
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

        init.when(CharConstraint.Builder.eq('\'')).goTo(gotFirstQuote);
        gotFirstQuote.when(CharConstraint.Builder.eq('\\')).goTo(escaped);
        gotFirstQuote.when(CharConstraint.Builder.and(CharConstraint.Builder.inRange(0x20, 128), CharConstraint.Builder.not(CharConstraint.Builder.eq('\\')))).goTo(gotChar);
        escaped.when(CharConstraint.Builder.inList("\"?abfnrtv\\")).goTo(gotChar);

        escaped.when(CharConstraint.Builder.inList(Lexemes.OctalDigit)).goTo(octalEscape);
        octalEscape.when(CharConstraint.Builder.inList(Lexemes.OctalDigit)).goTo(octalEscape2);
        octalEscape.when(CharConstraint.Builder.eq('\'')).goTo(done);
        octalEscape2.when(CharConstraint.Builder.inList(Lexemes.OctalDigit)).goTo(gotChar);
        octalEscape2.when(CharConstraint.Builder.eq('\'')).goTo(done);

        escaped.when(CharConstraint.Builder.eq('x')).goTo(hexEscape);
        escaped.when(CharConstraint.Builder.inList(Lexemes.HexDigit)).goTo(hexEscape);
        hexEscape.when(CharConstraint.Builder.eq('\'')).goTo(done);

        escaped.when(CharConstraint.Builder.or(CharConstraint.Builder.eq('u'), CharConstraint.Builder.eq('U'))).goTo(universalEscape);
        gotHexQuad = addHexQuad(builder, universalEscape);
        gotHexQuad.when(CharConstraint.Builder.eq('\'')).goTo(done);
        gotHexQuad2 = addHexQuad(builder, gotHexQuad);
        gotHexQuad2.when(CharConstraint.Builder.eq('\'')).goTo(done);

        gotChar.when(CharConstraint.Builder.eq('\'')).goTo(done);
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

    public static CCharacter unMarshall(java.io.DataInput in) throws IOException {
        return TerminalImpl.unMarshall(new CCharacter(), in);
    }

    @Override
    public String toString() {
        return "cCharacter";
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

}
