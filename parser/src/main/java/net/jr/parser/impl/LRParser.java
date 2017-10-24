package net.jr.parser.impl;

import net.jr.collection.iterators.Iterators;
import net.jr.collection.iterators.PushbackIterator;
import net.jr.lexer.Token;
import net.jr.parser.Grammar;
import net.jr.parser.Parser;
import net.jr.parser.Rule;
import net.jr.parser.errors.ParseException;

import java.util.Iterator;
import java.util.Stack;

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

    public void parse(Iterator<Token> it) {

        final PushbackIterator<Token> tokenIterator;
        if(it instanceof PushbackIterator) {
            tokenIterator = (PushbackIterator<Token>)it;
        } else {
            tokenIterator = Iterators.pushbackIterator(it);
        }

        Stack<Integer> stack = new Stack<>();

        //start with the initial state
        stack.push(0);

        //repeat until done
        boolean completed = false;
        while (!completed) {

            int currentState = stack.peek();
            Token token = tokenIterator.next();
            Action decision = actionTable.getAction(currentState, token.getTokenType());
            if (decision == null) {
                throw new IllegalStateException("Internal parser error ! token="+token.getTokenType());
            }

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
                    tokenIterator.pushback(token);
                    break;
                default:
                    throw new IllegalStateException(String.format("Illegal action type '%s' !", decision.getActionType().name()));
            }
        }
    }

    protected void reduce(Stack<Integer> stack, int ruleIndex) {
        // for each symbol on the left side of the rule, a state is removed from the stack
        Rule rule = grammar.getRuleById(ruleIndex);
        for (int i = 0; i < rule.getClause().length; i++) {
            stack.pop();
        }
        // depending state that is now on the top of stack, and the target of the rule,
        // a new state is searched in the goto table and becomes the current state
        int newState = actionTable.getNextState(stack.peek(), rule.getTarget());
        stack.push(newState);
    }

    public Grammar getGrammar() {
        return grammar;
    }
}
