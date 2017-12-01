package net.jr.lexer.expr.impl;

import net.jr.lexer.Lexeme;
import net.jr.lexer.impl.Automaton;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

public class RegexAutomaton implements Automaton {

    private Lexeme tokenType;

    private Node startNode;

    private Set<Node> activeNodes;

    private boolean inFinalState = false;

    private int matchLen = 0;

    public RegexAutomaton(Node startNode) {
        this.startNode = startNode;
    }

    @Override
    public boolean step(char c) {
        inFinalState = false;
        Set<Node> newActiveNodes = new HashSet<>();
        for (Node node : activeNodes) {
            for (Transition t : node.getOutgoingTransitions()) {
                if (t.getCharConstraint().apply(c)) {
                    Node target = t.getTarget();
                    inFinalState = inFinalState || target.isFinalState();
                    newActiveNodes.add(target);
                }
            }
        }
        activeNodes = newActiveNodes;
        boolean dead = activeNodes.isEmpty();
        if(!dead) {
            matchLen++;
        }
        return dead;
    }

    @Override
    public void reset() {
        matchLen = 0;
        activeNodes = new HashSet<>();
        activeNodes.add(startNode);
    }

    @Override
    public int getMatchedLength() {
        return matchLen;
    }

    @Override
    public boolean isInFinalState() {
        return inFinalState;
    }

    @Override
    public Lexeme getTokenType() {
        return tokenType;
    }

    public void setTokenType(Lexeme tokenType) {
        this.tokenType = tokenType;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        RegexAutomaton regexAutomaton = new RegexAutomaton(startNode);
        regexAutomaton.setTokenType(tokenType);
        return regexAutomaton;
    }


    public String toGraphviz() {
        int i = 0;
        Map<String, Node> nodes = new TreeMap<>();
        Stack<Node> s = new Stack<>();
        s.push(startNode);

        while(!s.isEmpty()) {
            Node n = s.pop();
            if(!nodes.containsValue(n)) {
                nodes.put(Integer.toString(i++), n);
                for (Transition t : n.getOutgoingTransitions()) {
                    Node target = t.getTarget();
                    s.push(target);
                }
            }
        }

        Map<Node, String> rev = new HashMap<>();
        for(Map.Entry<String, Node> entry : nodes.entrySet()) {
            rev.put(entry.getValue(), entry.getKey());
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("digraph " + getClass().getSimpleName() +" {");
        pw.println("rankdir=LR;");
        pw.println("size=\"8,5\";");
        for(Map.Entry<String, Node> entry : nodes.entrySet()) {
            String nodeName = entry.getKey();
            Node node = entry.getValue();
            if(node.isFinalState()) {
                pw.println(String.format("%s [ peripheries = 2 ];", nodeName));
            }
            for(Transition t : entry.getValue().getOutgoingTransitions()) {
                String targetName = rev.get(t.getTarget());
                pw.println(String.format("%s -> %s [ label = \"%s\" ];", nodeName, targetName, t.getCharConstraint().toString()));
            }
        }

        pw.println("}");
        pw.flush();
        return sw.toString();
    }

}
