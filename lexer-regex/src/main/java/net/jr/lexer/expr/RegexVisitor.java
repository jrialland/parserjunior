package net.jr.lexer.expr;

import net.jr.parser.ast.AstNode;
import net.jr.parser.ast.annotations.Target;

import java.util.*;

import static net.jr.lexer.impl.CharConstraint.Builder.*;

public class RegexVisitor {

    private static class Context {
        private Node start;

        private Node end;
    }

    private LinkedList<Context> stack = new LinkedList<>();

    private RegexLexeme regexLexeme;

    public RegexVisitor(RegexLexeme regexLexeme) {
        this.regexLexeme = regexLexeme;
    }

    public RegexAutomaton getAutomaton() {
        Context context = stack.pop();
        context.end.setFinalState(true);
        return new RegexAutomaton(context.start);
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

    @Target("Regex")
    @SuppressWarnings("unused")
    public void visitRegex(AstNode node) {

        LinkedList<Context> list = new LinkedList<>(stack);
        Collections.reverse(list);
        Context first = list.removeFirst();
        Context current = first;

        for(Context context : list) {
            for(Transition t : current.end.getIncomingTransitions()) {
                t.setTarget(context.start);
            }
            current = context;
        }
        first.end = current.end;
        stack.clear();
        stack.push(first);
    }

    /**
     * a character sequence ('word')
     * @param node
     */
    @Target("CharSequence")
    @SuppressWarnings("unused")
    public void visitCharSequence(AstNode node) {
        String sequence = node.asToken().getText();
        Iterator<Character> iterator = getChars(sequence).iterator();
        Context context = new Context();
        Node current = new Node();
        context.start = current;

        while (iterator.hasNext()) {
            Character c = iterator.next();
            Node n = new Node();
            current.addTransition(eq(c)).toNode(n);
            current = n;
        }
        context.end = current;
        context.end.setFinalState(true);
        stack.push(context);
    }

    /**
     * A character range ('0'..'9')
     * @param node
     */
    @Target("Range")
    @SuppressWarnings("unused")
    public void visitRange(AstNode node) {
        node = node.getFirstChild();
        List<AstNode> children = node.getChildrenOfType(RegexGrammar.Tokens.Char);
        String strLower = children.get(0).asToken().getText();
        String strUpper = children.get(1).asToken().getText();
        char l = getChars(strLower).get(0);
        char u = getChars(strUpper).get(0);
        Context context = new Context();
        context.start = new Node();
        context.end = new Node();
        context.start.addTransition(inRange(l, u)).toNode(context.end);
        context.end.setFinalState(true);
        stack.push(context);
    }

    /**
     * A character ('x')
     * @param node
     */
    @Target("Char")
    @SuppressWarnings("unused")
    public void visitChar(AstNode node) {
        Character c = getChars(node.asToken().getText()).get(0);
        Context context = new Context();
        context.start = new Node();
        context.end = new Node();
        context.start.addTransition(eq(c)).toNode(context.end);
        context.end.setFinalState(true);
        stack.push(context);
    }

    /**
     * The '.' character
     */
    @Target("AnyChar")
    @SuppressWarnings("unused")
    public void visitAnychar(AstNode node) {
        Context context = new Context();
        context.start = new Node();
        context.end = new Node();
        context.end.setFinalState(true);
        context.start.addTransition(any()).toNode(context.end);
        stack.push(context);
    }

    @Target("Optional")
    @SuppressWarnings("unused")
    public void visitOptional(AstNode node) {
        Context context = stack.peek();
        context.start.setFinalState(true);
    }

    @Target("ZeroOrMore")
    @SuppressWarnings("unused")
    public void visitZeroOrMore(AstNode node) {
        Context context = stack.peek();
        context.start.setFinalState(true);//having nothing is ok
        for(Transition t : context.end.getIncomingTransitions()) {
            t.setTarget(context.start);
        }
        context.end = context.start;
    }

    @Target("OneOrMore")
    @SuppressWarnings("unused")
    public void visitOneOrMore(AstNode node) {
        Context context = stack.peek();
        Node n2 = new Node();
        for(Transition t : context.start.getOutgoingTransitions()) {
            n2.addTransition(t.getCharConstraint()).toNode(t.getTarget());
        }
        for(Transition t : context.end.getIncomingTransitions()) {
            t.setTarget(n2);
        }
        context.end = n2;
    }

    @Target("Or")
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

        for (Transition t : left.end.getIncomingTransitions()) {
            t.setTarget(lastNode);
        }

        for (Transition t : right.end.getIncomingTransitions()) {
            t.setTarget(lastNode);
        }
        lastNode.setFinalState(true);
        Context context = new Context();
        context.start = firstNode;
        context.end = lastNode;
        stack.push(context);
    }
}
