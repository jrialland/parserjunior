package net.jr.lexer.expr.impl;

import net.jr.lexer.Lexeme;
import net.jr.lexer.automaton.Automaton;
import net.jr.lexer.automaton.State;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

public class RegexAutomaton implements Automaton {

    private Lexeme tokenType;

    private Node startNode;

    public RegexAutomaton(Node startNode) {
        this.startNode = startNode;
    }

    @Override
    public State getInitialState() {
        return startNode;
    }

    @Override
    public Lexeme getTokenType() {
        return tokenType;
    }

    public void setTokenType(Lexeme tokenType) {
        this.tokenType = tokenType;
    }

    @Override
    public Object clone() {
        RegexAutomaton regexAutomaton = new RegexAutomaton(startNode);
        regexAutomaton.setTokenType(tokenType);
        return regexAutomaton;
    }

    public String toGraphviz() {
        int i = 0;
        Map<String, Node> nodes = new TreeMap<>();
        Stack<Node> s = new Stack<>();
        s.push(startNode);

        while (!s.isEmpty()) {
            Node n = s.pop();
            if (!nodes.containsValue(n)) {
                nodes.put(Integer.toString(i++), n);
                for (net.jr.lexer.automaton.Transition _t : n.getOutgoingTransitions()) {
                    Transition t = (Transition)_t;
                    Node target = t.getTarget();
                    s.push(target);
                }
            }
        }

        Map<Node, String> rev = new HashMap<>();
        for (Map.Entry<String, Node> entry : nodes.entrySet()) {
            rev.put(entry.getValue(), entry.getKey());
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("digraph " + getClass().getSimpleName() + " {");
        pw.println("rankdir=LR;");
        pw.println("size=\"8,5\";");
        for (Map.Entry<String, Node> entry : nodes.entrySet()) {
            String nodeName = entry.getKey();
            Node node = entry.getValue();
            if (node.isFinalState()) {
                pw.println(String.format("%s [ peripheries = 2 ];", nodeName));
            }
            for (net.jr.lexer.automaton.Transition _t : entry.getValue().getOutgoingTransitions()) {
                Transition t = (Transition)_t;
                String targetName = rev.get(t.getTarget());
                pw.println(String.format("%s -> %s [ label = \"%s\" ];", nodeName, targetName, t.getCharConstraint().toString()));
            }
        }

        pw.println("}");
        pw.flush();
        return sw.toString();
    }

}
