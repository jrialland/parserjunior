package net.jr.lexer.expr.impl;

import net.jr.lexer.expr.RegexLexeme;
import net.jr.parser.ast.AstNode;
import net.jr.parser.ast.annotations.After;
import net.jr.parser.ast.annotations.Before;

import java.util.*;

import static net.jr.lexer.impl.CharConstraint.Builder.*;

public class RegexVisitor {

    private static class Context {

        private Node start;

        private Node end;

        public Context(Node start, Node end) {
            this.start = start;
            this.end = end;
        }

        public Node getStart() {
            return start;
        }

        public Node getEnd() {
            return end;
        }
    }

    private LinkedList<Context> stack = new LinkedList<>();

    private LinkedList<Integer> stackSize = new LinkedList<>();

    private RegexLexeme regexLexeme;

    public RegexVisitor(RegexLexeme regexLexeme) {
        this.regexLexeme = regexLexeme;
    }

    public RegexAutomaton getAutomaton() {
        Context context = stack.pop();
        context.end.setFinalState(true);
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
            Context next = stack.pop();
            Context first = stack.pop();

            Node mergeNode = new Node();
            List<Transition> transitions;

            // connections that used to go to first.end now go to mergeNode
            transitions = new ArrayList<>(first.end.getIncomingTransitions());
            transitions.forEach(t -> t.setTarget(mergeNode));

            //connections that used to originate from first.end now originate from mergeNode
            transitions = new ArrayList<>(first.end.getOutgoingTransitions());
            transitions.forEach(t -> t.setSource(mergeNode));

            //connections that used to go ot next.start now go to mergeNode
            transitions = new ArrayList<>(next.start.getIncomingTransitions());
            transitions.forEach(t -> t.setTarget(mergeNode));

            //connections that used originate from next.start now originate from mergeNode
            transitions = new ArrayList<>(next.start.getOutgoingTransitions());
            transitions.forEach(t -> t.setSource(mergeNode));

            stack.push(new Context(first.getStart(), next.getEnd()));
            items--;
        }
    }

    /**
     * a character sequence ('word')
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
        current.setFinalState(true);
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
        end.setFinalState(true);
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
        end.setFinalState(true);
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
        end.setFinalState(true);
        start.addTransition(any()).toNode(end);
        stack.push(new Context(start, end));
    }

    @After("Optional")
    @SuppressWarnings("unused")
    public void visitOptional(AstNode node) {
        Context context = stack.peek();
        context.start.setFinalState(true);
    }

    @After("ZeroOrMore")
    @SuppressWarnings("unused")
    public void visitZeroOrMore(AstNode node) {
        Context context = stack.peek();
        context.start.setFinalState(true);//having nothing is ok
        List<Transition> list = new ArrayList<>(context.end.getIncomingTransitions());
        for (Transition t : list) {
            t.setTarget(context.start);
        }
        context.end = context.start;
    }

    @After("OneOrMore")
    @SuppressWarnings("unused")
    public void visitOneOrMore(AstNode node) {
        Context context = stack.peek();
        Node n2 = new Node();
        for (Transition t : context.start.getOutgoingTransitions()) {
            n2.addTransition(t.getCharConstraint()).toNode(t.getTarget());
        }
        Set<Transition> incoming = new HashSet<>(context.end.getIncomingTransitions());
        for (Transition t : incoming) {
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

        for (Transition t : left.start.getOutgoingTransitions()) {
            firstNode.addTransition(t.getCharConstraint()).toNode(t.getTarget());
        }
        for (Transition t : right.start.getOutgoingTransitions()) {
            firstNode.addTransition(t.getCharConstraint()).toNode(t.getTarget());
        }
        left.start.disconnect();
        right.start.disconnect();

        Set<Transition> incomingToEnd = new HashSet<>(left.end.getIncomingTransitions());
        incomingToEnd.addAll(right.end.getIncomingTransitions());

        for (Transition t : incomingToEnd) {
            t.setTarget(lastNode);
        }

        if (right.end.isFinalState() || left.end.isFinalState()) {
            lastNode.setFinalState(true);
        }

        right.end.disconnect();
        left.end.disconnect();

        lastNode.setFinalState(true);
        stack.push(new Context(firstNode, lastNode));
    }
}
