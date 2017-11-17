package net.jr.lexer.impl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Artificial extends LexemeImpl {

    private String name;

    public Artificial(String name) {
        this.name = name;
        setAutomaton(FailAutomaton.get(this));
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

    public static Artificial unMarshall(DataInputStream in) throws IOException {
        return new Artificial(in.readUTF());
    }
}
