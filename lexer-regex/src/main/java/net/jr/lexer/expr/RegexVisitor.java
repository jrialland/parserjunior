package net.jr.lexer.expr;

import net.jr.lexer.impl.Automaton;
import net.jr.lexer.impl.CharConstraint;
import net.jr.lexer.impl.DefaultAutomaton;
import net.jr.parser.ast.AstNode;
import net.jr.parser.ast.annotations.Target;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import static net.jr.lexer.impl.CharConstraint.Builder.eq;

public class RegexVisitor {

    private Stack<Automaton> stack = new Stack<>();

    private RegexLexeme regexLexeme;

    private DefaultAutomaton.Builder automatonBuilder;

    private DefaultAutomaton.Builder.BuilderState initialState;

    private DefaultAutomaton.Builder.BuilderState currentState;

    public RegexVisitor(RegexLexeme regexLexeme) {
        this.regexLexeme = regexLexeme;
        automatonBuilder = DefaultAutomaton.Builder.forTokenType(regexLexeme);
        initialState = automatonBuilder.initialState();
        currentState = initialState;
    }

    public Automaton getAutomaton() {
        return automatonBuilder.build();
    }

    /**
     * get each individual char from a 'xxxxx' sequence.
     * FIXME : handle escape sequences
     * @param sequence
     * @return
     */
    private List<Character> getChars(String sequence) {
        //remove quotes
        sequence = sequence.replaceAll("^'(.*)'$", "$1");
        List<Character> list = new ArrayList<>();
        for(char c : sequence.toCharArray()) {
            list.add(c);
        }
        return list;
    }

    @Target("CharSequence")
    @SuppressWarnings("unused")
    public void visitCharSequence(AstNode node) {
        String sequence = node.asToken().getText();
        Iterator<Character> iterator = getChars(sequence).iterator();
        while(iterator.hasNext()) {
            Character c = iterator.next();
            final DefaultAutomaton.Builder.BuilderState targetState;
            if (iterator.hasNext()) {
                targetState = automatonBuilder.newNonFinalState();
            } else {
                targetState = automatonBuilder.newFinalState();
            }
            currentState.when(eq(c)).goTo(targetState);
            currentState = targetState;
        }
    }

    @Target("Range")
    @SuppressWarnings("unused")
    public void visitRange(AstNode node) {
        node = node.getFirstChild();
        List<AstNode> children = node.getChildrenOfType(RegexGrammar.Tokens.Char);
        String strLower = children.get(0).asToken().getText();
        String strUpper = children.get(1).asToken().getText();
        char l = getChars(strLower).get(0);
        char u = getChars(strUpper).get(0);
        DefaultAutomaton.Builder.BuilderState destination = automatonBuilder.newFinalState();
        currentState.when(CharConstraint.Builder.inRange(l, u)).goTo(destination);
        currentState = destination;
    }


    public void visitOptional() {

    }


    public void visitZeroOrMore() {

    }

    public void visitOneOrMore() {

    }

    @Target("Or")
    public void visitOr(AstNode node) {
        System.out.println("visit or !!!!!!");
    }


    public void visitRepetition() {

    }


    public void RepetitionWithBounds() {

    }


    public void visitAnychar() {

    }

    public void visitGroup() {

    }
}
