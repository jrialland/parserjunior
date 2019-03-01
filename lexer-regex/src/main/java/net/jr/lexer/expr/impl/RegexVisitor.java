package net.jr.lexer.expr.impl;

import net.jr.lexer.expr.RegexTerminal;
import net.jr.lexer.impl.CharConstraint;
import net.jr.parser.ast.AstNode;
import net.jr.parser.ast.annotations.After;
import net.jr.parser.ast.annotations.Before;

import java.util.*;

import static net.jr.lexer.impl.CharConstraint.Builder.*;

public class RegexVisitor {

    private LinkedList<Context> stack = new LinkedList<>();
    private LinkedList<Integer> stackSize = new LinkedList<>();
    private RegexTerminal regexLexeme;

    public RegexVisitor(RegexTerminal regexLexeme) {
        this.regexLexeme = regexLexeme;
    }

    public RegexTerminal getRegexLexeme() {
        return regexLexeme;
    }

    public RegexAutomaton getAutomaton() {
        Context context = stack.pop();
        context.end.setTerminal(regexLexeme);
        return new RegexAutomaton(context.getStart());
    }

    /**
     * Get each individual char from a 'xxxxx' sequence.
     * FIXME : handle escape sequences
     *
     * @param sequence
     * @return
     */
    private List<Character> getChars(String sequence) {
        //remove quotes
        sequence = sequence.replaceAll("^'(.*)'$", "$1");
        List<Character> list = new ArrayList<>();
        for (char c : sequence.toCharArray()) {
            list.add(c);
        }
        return list;
    }

    @Before("Regex")
    public void beforeRegex(AstNode node) {
        stackSize.push(0);
    }

    @After("Regex")
    @SuppressWarnings("unused")
    public void visitRegex(AstNode node) {
        visitGroup(node);
    }

    @Before("Group")
    public void beforeGroup(AstNode node) {
        stackSize.push(stack.size());
    }

    @After("Group")
    @SuppressWarnings("unused")
    public void visitGroup(AstNode node) {

        int items = stack.size() - stackSize.pop();

        while (items > 1) {
            Context right = stack.pop();
            Context left = stack.pop();

            Node mergeNode = new Node();

            mergeNode.setFallbackTransition(right.start.getFallbackTransition());
            mergeNode.setTerminal(right.start.getTerminal());

            Set<net.jr.lexer.automaton.Transition> transitions;

            // connections that used to go to first.end now go to mergeNode
            transitions = new HashSet<>(left.end.getIncomingTransitions());
            transitions.forEach(t -> {
                ((Transition) t).setTarget(mergeNode);
            });

            //connections that used to originate from first.end now originate from mergeNode
            transitions = new HashSet<>(left.end.getOutgoingTransitions());
            transitions.forEach(t -> ((Transition) t).setSource(mergeNode));

            //connections that used to go ot next.start now go to mergeNode
            transitions = new HashSet<>(right.start.getIncomingTransitions());
            transitions.forEach(t -> ((Transition) t).setTarget(mergeNode));

            //connections that used originate from next.start now originate from mergeNode
            transitions = new HashSet<>(right.start.getOutgoingTransitions());
            transitions.forEach(t -> ((Transition) t).setSource(mergeNode));

            Context grouped = new Context(left.getStart(), right.getEnd());
            stack.push(grouped);
            items--;
        }
    }

    /**
     * A character sequence ('word')
     *
     * @param node
     */
    @After("CharSequence")
    @SuppressWarnings("unused")
    public void visitCharSequence(AstNode node) {
        String sequence = node.asToken().getText();
        Iterator<Character> iterator = getChars(sequence).iterator();
        Node start = new Node();
        Node current = start;

        while (iterator.hasNext()) {
            Character c = iterator.next();
            Node n = new Node();
            current.addTransition(eq(c)).toNode(n);
            current = n;
        }
        current.setTerminal(regexLexeme);
        stack.push(new Context(start, current));
    }

    /**
     * A character range ('0'..'9')
     *
     * @param node
     */
    @After("Range")
    @SuppressWarnings("unused")
    public void visitRange(AstNode node) {
        node = node.getFirstChild();
        List<AstNode> children = node.getChildrenOfType(RegexGrammar.Tokens.Char);
        String strLower = children.get(0).asToken().getText();
        String strUpper = children.get(1).asToken().getText();
        char l = getChars(strLower).get(0);
        char u = getChars(strUpper).get(0);
        Node start = new Node();
        Node end = new Node();
        start.addTransition(inRange(l, u)).toNode(end);
        end.setTerminal(regexLexeme);
        stack.push(new Context(start, end));
    }

    /**
     * A character ('x')
     *
     * @param node
     */
    @After("Char")
    @SuppressWarnings("unused")
    public void visitChar(AstNode node) {
        Character c = getChars(node.asToken().getText()).get(0);
        Node start = new Node();
        Node end = new Node();
        start.addTransition(eq(c)).toNode(end);
        end.setTerminal(regexLexeme);
        stack.push(new Context(start, end));
    }

    /**
     * The '.' character
     */
    @After("AnyChar")
    @SuppressWarnings("unused")
    public void visitAnychar(AstNode node) {
        Node start = new Node();
        Node end = new Node();
        end.setTerminal(regexLexeme);
        start.addTransition(any()).toNode(end);
        stack.push(new Context(start, end));
    }

    @After("Optional")
    @SuppressWarnings("unused")
    public void visitOptional(AstNode node) {
        Context context = stack.peek();
        Transition skipTransition = (Transition) context.start.addFallback();
        skipTransition.setTarget(context.end);
    }

    @After("ZeroOrMore")
    @SuppressWarnings("unused")
    public void visitZeroOrMore(AstNode node) {
        Context context = stack.peek();
        context.start.setTerminal(regexLexeme);//having nothing is ok
        List<net.jr.lexer.automaton.Transition> list = new ArrayList<>(context.end.getIncomingTransitions());
        for (net.jr.lexer.automaton.Transition _t : list) {
            Transition t = (Transition) _t;
            t.setTarget(context.start);
        }
        context.end = context.start;
    }

    @After("OneOrMore")
    @SuppressWarnings("unused")
    public void visitOneOrMore(AstNode node) {
        Context context = stack.peek();
        Node n2 = new Node();
        for (net.jr.lexer.automaton.Transition _t : context.start.getOutgoingTransitions()) {
            Transition t = (Transition) _t;
            CharConstraint charConstraint = ((CharConstraint) t.getConstraint());
            n2.addTransition(charConstraint).toNode(t.getTarget());
        }
        Set<net.jr.lexer.automaton.Transition> incoming = new HashSet<>(context.end.getIncomingTransitions());
        for (net.jr.lexer.automaton.Transition _t : incoming) {
            Transition t = (Transition) _t;
            t.setTarget(n2);
        }
        context.end = n2;
    }

    @After("Or")
    @SuppressWarnings("unused")
    public void visitOr(AstNode node) {
        Context right = stack.pop();
        Context left = stack.pop();

        Node firstNode = new Node();
        Node lastNode = new Node();

        for (net.jr.lexer.automaton.Transition _t : left.start.getOutgoingTransitions()) {
            Transition t = (Transition) _t;
            CharConstraint charConstraint = (CharConstraint) t.getConstraint();
            firstNode.addTransition(charConstraint).toNode(t.getTarget());
        }
        for (net.jr.lexer.automaton.Transition _t : right.start.getOutgoingTransitions()) {
            Transition t = (Transition) _t;
            CharConstraint charConstraint = (CharConstraint) t.getConstraint();
            firstNode.addTransition(charConstraint).toNode(t.getTarget());
        }
        left.start.disconnect();
        right.start.disconnect();

        Set<net.jr.lexer.automaton.Transition> incomingToEnd = new HashSet<>(left.end.getIncomingTransitions());
        incomingToEnd.addAll(right.end.getIncomingTransitions());

        for (net.jr.lexer.automaton.Transition _t : incomingToEnd) {
            Transition t = (Transition) _t;
            t.setTarget(lastNode);
        }

        right.end.disconnect();
        left.end.disconnect();

        lastNode.setTerminal(regexLexeme);
        stack.push(new Context(firstNode, lastNode));
    }

    private static class Context {

        private Node start;

        private Node end;

        Context(Node start, Node end) {
            this.start = start;
            this.end = end;
        }

        Node getStart() {
            return start;
        }

        Node getEnd() {
            return end;
        }
    }
}
