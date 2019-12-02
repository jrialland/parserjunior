package net.jr.lexer.basicterminals;

import net.jr.lexer.automaton.FailAutomaton;
import net.jr.lexer.impl.TerminalImpl;

import java.io.IOException;

public class Artificial extends TerminalImpl {

    public Artificial(String name) {
        this();
        setName(name);
        setAutomaton(FailAutomaton.get(this));
    }

    private Artificial() {
        super();
    }

    public static Artificial unMarshall(java.io.DataInput in) throws IOException {
        return TerminalImpl.unMarshall(new Artificial(), in);
    }

    @Override
    public String toString() {
        return getName();
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
        return getName().equals(o.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode() ^ 541;
    }

}
