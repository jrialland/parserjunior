package net.jr.lexer.basicterminals;

import net.jr.lexer.automaton.Automaton;
import net.jr.lexer.automaton.DefaultAutomaton;
import net.jr.lexer.impl.CharConstraint;
import net.jr.lexer.impl.TerminalImpl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Word extends TerminalImpl {

    private Automaton automaton;

    private String name;

    private String possibleFirstChar, possibleNextChars;

    public Word(String possibleChars) {
        this(possibleChars, possibleChars);
    }

    public Word(String possibleFirstChar, String possibleNextChars) {
        this(possibleFirstChar, possibleNextChars, null);
    }

    public Word(String possibleFirstChar, String possibleNextChars, String name) {
        this.possibleFirstChar = possibleFirstChar;
        this.possibleNextChars = possibleNextChars;
        DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(this);
        DefaultAutomaton.Builder.BuilderState ok = builder.newFinalState();
        builder.initialState().when(CharConstraint.Builder.inList(possibleFirstChar)).goTo(ok);
        ok.when(CharConstraint.Builder.inList(possibleNextChars)).goTo(ok);
        this.automaton = builder.build();
        if (name == null) {
            if (possibleFirstChar.equals(possibleNextChars)) {
                this.name = String.format("Word('%s')", possibleFirstChar);
            } else {
                this.name = String.format("Word('%s', '%s')", possibleFirstChar, possibleNextChars);
            }
        } else {
            this.name = name;
        }
    }

    @SuppressWarnings("unused")
    public static Word unMarshall(DataInputStream dataInputStream) throws IOException {
        String name = dataInputStream.readUTF();
        String possibleFirstChar = dataInputStream.readUTF();
        String possibleNextChars = dataInputStream.readUTF();
        return new Word(possibleFirstChar, possibleNextChars, name);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!obj.getClass().equals(Word.class)) {
            return false;
        }
        final Word o = (Word) obj;
        return possibleFirstChar.equals(o.possibleFirstChar) && possibleNextChars.equals(o.possibleNextChars);
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public Automaton getAutomaton() {
        return automaton;
    }

    @Override
    public void marshall(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeUTF(name);
        dataOutputStream.writeUTF(possibleFirstChar);
        dataOutputStream.writeUTF(possibleNextChars);
    }
}
