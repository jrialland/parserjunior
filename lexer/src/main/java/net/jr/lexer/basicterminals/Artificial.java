package net.jr.lexer.basicterminals;

import net.jr.lexer.automaton.FailAutomaton;
import net.jr.lexer.impl.TerminalImpl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Artificial extends TerminalImpl {

    private String name;

    public Artificial(String name) {
        this.name = name;
        setAutomaton(FailAutomaton.get(this));
    }

    public static Artificial unMarshall(DataInputStream in) throws IOException {
        return new Artificial(in.readUTF());
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!obj.getClass().equals(Artificial.class)) {
            return false;
        }
        final Artificial o = (Artificial) obj;
        return name.equals(o.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode() ^ 541;
    }

    @Override
    public void marshall(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeUTF(name);
    }
}
