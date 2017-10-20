package net.jr.parser.impl;

import net.jr.lexer.Lexeme;
import net.jr.lexer.Lexer;
import net.jr.lexer.Token;
import net.jr.parser.Grammar;
import net.jr.parser.Parser;
import net.jr.parser.Rule;
import net.jr.parser.errors.ParseException;

import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * Implementation of the LR parser algorithm.
 */
public class LRParser implements Parser {

    private Grammar grammar;

    private ActionTable actionTable;

    /**
     * @param grammar
     * @param actionTable The actionTable for the grammar, possibly computed using {@link ActionTable.LALR1Builder#build(Grammar, Rule)}
     */
    public LRParser(Grammar grammar, ActionTable actionTable) {
        this.grammar = grammar;
        this.actionTable = actionTable;
    }

    /**
     * Gets the list of all terminals
     *
     * @return
     */
    protected List<Lexeme> getLexemes() {
        return grammar.getTerminals().stream().map(s -> (Lexeme) s).collect(Collectors.toList());
    }

    /**
     * Get the iterator that will feed the parser with terminals
     *
     * @param in
     * @return
     */
    protected Iterator<Token> getLexerIterator(Reader in) {
        return new Lexer(getLexemes()).iterator(in);
    }

    @Override
    public void parse(Reader in) {
        Stack<Integer> stack = new Stack<>();
        Iterator<Token> lexer = getLexerIterator(in);

        System.out.println(grammar);
        System.out.println(actionTable);

        //start with the initial state
        stack.push(0);

        //repeat until done
        boolean completed = false;
        while (!completed) {

            int currentState = stack.peek();
            Token token = lexer.next();
            System.out.println("------------------------");
            System.out.println(stack);
            System.out.println("Current state : " + currentState +", token type = " + token.getTokenType());
            Action decision = actionTable.getAction(currentState, token.getTokenType());
            if (decision == null) {
                throw new IllegalStateException("Internal parser error !");
            }

            System.out.println("Decision :  " + decision);

            switch (decision.getActionType()) {
                case Accept:
                    completed = true;
                    break;
                case Fail:
                    throw new ParseException(token, actionTable.getExpectedLexemes(currentState));
                case Shift:
                    //new state is added on the stack, so it becomes the current state
                    stack.add(decision.getActionParameter());
                    break;
                case Reduce:
                    reduce(stack, decision.getActionParameter());
                    break;
                default:
                    throw new IllegalStateException(String.format("Illegal action type '%s' !", decision.getActionType().name()));
            }
        }
    }

    protected void reduce(Stack<Integer> stack, int ruleIndex) {

        // for each symbol on the left side of the rule, a state is removed from the stack
        Rule rule = grammar.getRuleById(ruleIndex);
        System.out.println("    removing " + rule.getClause().length + " items from stack");
        for(int i=0; i<rule.getClause().length; i++) {
            stack.pop();
        }

        System.out.println("    Stack is now" + stack);

        System.out.println("    Reduction symbol is " + rule.getTarget());

        // depending state that is now on the top of stack, and the target of the rule,
        // a new state is searched in the goto table and becomes the current state
        int newState = actionTable.getNextState(stack.pop(), rule.getTarget());
        System.out.println("    Reduce go to state " + newState);
        stack.push(newState);
    }

    public Grammar getGrammar() {
        return grammar;
    }
}
