package net.jr.lexer.expr.impl;

import net.jr.lexer.Terminal;
import net.jr.lexer.automaton.Automaton;
import net.jr.lexer.automaton.State;
import net.jr.lexer.impl.CharConstraint;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

public class RegexAutomaton implements Automaton {

    private Terminal tokenType;

    private Node startNode;

    public RegexAutomaton(Node startNode) {
        this.startNode = startNode;
    }

    @Override
    public State getInitialState() {
        return startNode;
    }

    @Override
    public Terminal getTokenType() {
        return tokenType;
    }

    public void setTokenType(Terminal tokenType) {
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
                    Transition t = (Transition) _t;
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
                Transition t = (Transition) _t;
                String targetName = rev.get(t.getTarget());
                String expr = ((CharConstraint)t.getConstraint()).getExpr();
                pw.println(String.format("%s -> %s [ label = \"%s\" ];", nodeName, targetName, expr));
            }

            Transition fallback;

            if((fallback = (Transition)entry.getValue().getFallbackTransition()) != null) {
                String targetName = rev.get(fallback.getTarget());
                String expr = ((CharConstraint)fallback.getConstraint()).getExpr();
                pw.println(String.format("%s -> %s [ style=dashed, label = \"%s\" ];", nodeName, targetName, expr));
            }

        }

        pw.println("}");
        pw.flush();
        return sw.toString();
    }

}
