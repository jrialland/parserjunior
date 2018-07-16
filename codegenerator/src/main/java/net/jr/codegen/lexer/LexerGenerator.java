package net.jr.codegen.lexer;

import net.jr.lexer.Lexer;
import net.jr.lexer.automaton.DefaultAutomaton;
import net.jr.lexer.automaton.State;
import net.jr.lexer.automaton.Transition;
import net.jr.lexer.impl.CharConstraint;
import net.jr.lexer.impl.MergingLexerStreamImpl;
import net.jr.text.IndentPrintWriter;
import net.jr.util.IOUtil;

import java.io.Writer;
import java.util.*;

public class LexerGenerator {

    private Map<Integer, State<Character>> getStates(Lexer lexer) {
        Map<Integer, State<Character>> allStates = new TreeMap<>();
        Set<State<Character>> viewed = new HashSet<>();
        State<Character> initialState = ((MergingLexerStreamImpl) lexer.iterator(IOUtil.emptyReader())).getInitialState();
        Stack<State<Character>> stack = new Stack<>();
        stack.push(initialState);
        int i = 0;
        while (!stack.isEmpty()) {
            State<Character> state = stack.pop();
            if(!viewed.contains(state)) {
                allStates.put(i++, state);
                viewed.add(state);
                for(Transition t : state.getOutgoingTransitions()) {
                    stack.push(t.getNextState());
                }
            }
        }
        return allStates;
    }

    public void generate(Lexer lexer, Writer writer) {

        IndentPrintWriter ipw = writer instanceof IndentPrintWriter ? (IndentPrintWriter) writer : new IndentPrintWriter(writer);

        ipw.println("switch(state) {");
        ipw.indent();

        for(Map.Entry<Integer, State<Character>> entry : getStates(lexer).entrySet()) {
            int id = entry.getKey();
            State<Character> state = entry.getValue();
            ipw.println("case " + id + ":");
            ipw.indent();

            boolean first = true;
            for(Transition<Character> t : state.getOutgoingTransitions()) {
                CharConstraint constraint = ((DefaultAutomaton.TransitionImpl)t).getCondition();
                ipw.println((first?"":"else ") + "if("+constraint.toString()+") {");
                ipw.indent();
                ipw.println("state = " + t.getNextState());
                ipw.deindent();
                ipw.println("}");
                first = false;
            }

            ipw.println("break;");
            ipw.deindent();

        }

        ipw.deindent();
        ipw.println("}");

        ipw.flush();
    }

}
