package net.jr.lexer.impl;

import net.jr.lexer.Lexeme;
import net.jr.marshalling.MarshallingCapable;
import net.jr.marshalling.MarshallingUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FailAutomaton implements Automaton, MarshallingCapable {

    private static Map<Lexeme, FailAutomaton> failAutomatonMap = new HashMap<>();

    private Lexeme lexeme;

    private FailAutomaton(Lexeme lexeme) {
        this.lexeme = lexeme;
    }

    @Override
    public boolean step(char c) {
        return true;
    }

    @Override
    public void reset() {

    }

    @Override
    public int getMatchedLength() {
        return 0;
    }

    @Override
    public boolean isInFinalState() {
        return false;
    }

    @Override
    public Lexeme getTokenType() {
        return lexeme;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return this;
    }

    @Override
    public void marshall(DataOutputStream dataOutputStream) throws IOException {
        lexeme.marshall(dataOutputStream);
    }

    @SuppressWarnings("unused")
    public static Automaton unMarshall(DataInputStream in) throws IOException {
        Lexeme l = MarshallingUtil.unMarshall(in);
        return get(l);
    }

    @Override
    public int hashCode() {
        return lexeme.hashCode() + 3;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!obj.getClass().equals(FailAutomaton.class)) {
            return false;
        }

        final FailAutomaton o = (FailAutomaton) obj;
        return o.lexeme.equals(lexeme);
    }

    public static FailAutomaton get(Lexeme lexeme) {
        return failAutomatonMap.computeIfAbsent(lexeme, l -> new FailAutomaton(l));
    }
}
