package net.jr.lexer.automaton;

import net.jr.lexer.Terminal;
import net.jr.marshalling.MarshallingCapable;
import net.jr.marshalling.MarshallingUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FailAutomaton implements Automaton, MarshallingCapable {

    private static Map<Terminal, FailAutomaton> failAutomatonMap = new HashMap<>();

    private Terminal terminal;

    private FailAutomaton(Terminal terminal) {
        this.terminal = terminal;
    }

    @Override
    public Terminal getTokenType() {
        return terminal;
    }

    @Override
    public Object clone() {
        return this;
    }

    @Override
    public void marshall(DataOutputStream dataOutputStream) throws IOException {
        terminal.marshall(dataOutputStream);
    }

    @SuppressWarnings("unused")
    public static Automaton unMarshall(DataInputStream in) throws IOException {
        Terminal l = MarshallingUtil.unMarshall(in);
        return get(l);
    }

    @Override
    public int hashCode() {
        return terminal.hashCode() + 3;
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
        return o.terminal.equals(terminal);
    }

    public static FailAutomaton get(Terminal terminal) {
        return failAutomatonMap.computeIfAbsent(terminal, l -> new FailAutomaton(l));
    }

    @Override
    public State getInitialState() {
        return null;
    }
}
