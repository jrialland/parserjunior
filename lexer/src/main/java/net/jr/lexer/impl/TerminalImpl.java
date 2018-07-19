package net.jr.lexer.impl;

import net.jr.common.SymbolBase;
import net.jr.lexer.Terminal;
import net.jr.lexer.automaton.Automaton;
import net.jr.lexer.basicterminals.Artificial;
import net.jr.marshalling.MarshallingUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class TerminalImpl extends SymbolBase implements Terminal {

    private Automaton automaton;

    protected Integer priority;

    protected String name;

    public TerminalImpl() {
        this(null);
    }

    public TerminalImpl(String name) {
        this.name = name;
        this.priority = 1;
    }

    @Override
    public int getPriority() {
        return priority == null ? 0 : priority;
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Automaton getAutomaton() {
        return automaton;
    }

    public void setAutomaton(Automaton automaton) {
        this.automaton = automaton;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        if (name != null) {
            return name;
        } else {
            return super.toString();
        }
    }

    @Override
    public Terminal withPriority(int priority) {
        try {
            TerminalImpl clone = MarshallingUtil.copyOf(this);
            clone.setPriority(priority);
            return clone;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void marshall(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(priority);
        dataOutputStream.writeBoolean(name != null);
        if(name != null) {
            dataOutputStream.writeUTF(name);
        }
    }

    public static <T extends TerminalImpl> T unMarshall(T impl, DataInputStream in) throws IOException {
        impl.priority = in.readInt();
        boolean hasName = in.readBoolean();
        if(hasName) {
            impl.name = in.readUTF();
        }
        return impl;
    }
}
