package net.jr.parser.impl;

import net.jr.collection.iterators.Iterators;
import net.jr.collection.iterators.PushbackIterator;
import net.jr.lexer.Lexeme;
import net.jr.lexer.Token;
import net.jr.parser.Grammar;
import net.jr.parser.ParseError;
import net.jr.parser.Parser;
import net.jr.parser.Rule;
import net.jr.parser.errors.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

/**
 * Implementation of the LR parser algorithm.
 */
public class LRParser implements Parser {

    private static final Logger LOGGER = LoggerFactory.getLogger(LRParser.class);

    public static Logger getLog() {
        return LOGGER;
    }

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

        getLog().debug("\n" + actionTable.toString());

        final PushbackIterator<Token> tokenIterator;
        if (it instanceof PushbackIterator) {
            tokenIterator = (PushbackIterator<Token>) it;
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
            getLog().debug("-> Current state : " + currentState);

            Token token = tokenIterator.next();
            getLog().debug("   Input token : " + token.getTokenType() + " (matched text : '" + token.getMatchedText() + "' )");

            Action decision = actionTable.getAction(currentState, token.getTokenType());
            if (decision == null) {
                Set<Lexeme> expected = actionTable.getExpectedLexemes(currentState);
                //if ε is was a possible 'symbol'
                if (expected.contains(Grammar.Empty)) {
                    decision = actionTable.getAction(currentState, Grammar.Empty);
                    tokenIterator.pushback(token);
                } else {
                    throw new ParseError(actionTable.getExpectedLexemes(currentState));
                }
            }

            getLog().debug(String.format("   Decision : %s %d", decision.getActionType().name(), decision.getActionParameter()));

            switch (decision.getActionType()) {
                case Accept:
                    completed = true;
                    break;
                case Fail:
                    throw new ParseException(token, actionTable.getExpectedLexemes(currentState));
                case Shift:
                    shift(stack, decision.getActionParameter());
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

    /**
     * The new state is added to the stack and becomes the current state
     *
     * @param stack
     * @param state
     */
    protected void shift(Stack<Integer> stack, int state) {
        stack.add(state);
    }

    protected void reduce(Stack<Integer> stack, int ruleIndex) {
        // for each symbol on the left side of the rule, a state is removed from the stack
        Rule rule = grammar.getRuleById(ruleIndex);
        getLog().debug("      - reducing rule : " + rule);

        for (int i = 0; i < rule.getClause().length; i++) {
            stack.pop();
        }

        // depending state that is now on the top of stack, and the target of the rule,
        // a new state is searched in the goto table and becomes the current state
        int newState = actionTable.getNextState(stack.peek(), rule.getTarget());
        getLog().debug("      - goto " + newState);
        stack.push(newState);
    }

    public Grammar getGrammar() {
        return grammar;
    }
}
