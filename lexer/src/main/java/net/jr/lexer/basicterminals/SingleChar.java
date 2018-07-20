package net.jr.lexer.basicterminals;


import net.jr.lexer.automaton.Automaton;
import net.jr.lexer.automaton.DefaultAutomaton;
import net.jr.lexer.impl.CharConstraint;
import net.jr.lexer.impl.TerminalImpl;

import java.io.*;

public class SingleChar extends TerminalImpl {

    private char character;

    public SingleChar(char character) {
        this.character = character;
    }

    private SingleChar() {

    }

    public static SingleChar unMarshall(DataInput dataInputStream) throws IOException {
        SingleChar s = TerminalImpl.unMarshall(new SingleChar(), dataInputStream);
        s.character = dataInputStream.readChar();
        return s;
    }

    @Override
    public Automaton getAutomaton() {
        DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(this);
        builder.initialState().when(CharConstraint.Builder.eq(character)).goTo(builder.newFinalState());
        return builder.build();
    }

    @Override
    public String toString() {
        String name = getName();
        return name == null ? "'" + Character.toString(character) + "'" : name;
    }

    @Override
    public int hashCode() {
        return character + 1844;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !obj.getClass().equals(SingleChar.class)) {
            return false;
        }
        return ((SingleChar) obj).character == character;
    }

    @Override
    public void marshall(DataOutput dataOutputStream) throws IOException {
        super.marshall(dataOutputStream);
        dataOutputStream.writeChar(character);
    }

}
