package net.jr.parser.impl;

import net.jr.collection.iterators.Iterators;
import net.jr.collection.iterators.PushbackIterator;
import net.jr.lexer.Lexeme;
import net.jr.lexer.Lexer;
import net.jr.lexer.Token;
import net.jr.parser.Grammar;
import net.jr.parser.ParseError;
import net.jr.parser.Parser;
import net.jr.parser.Rule;
import net.jr.parser.ast.AstNode;
import net.jr.parser.errors.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.util.*;

/**
 * Implementation of the LR parser algorithm.
 */
public class LRParser implements Parser {

    private static final Logger LOGGER = LoggerFactory.getLogger(LRParser.class);

    public static Logger getLog() {
        return LOGGER;
    }

    private Grammar grammar;

    private Lexer defaultLexer;

    private Rule targetRule;

    private ActionTable actionTable;

    private class ParserContext {

        private int state;

        private AstNode astNode;

        public void setState(int state) {
            this.state = state;
        }

        public int getState() {
            return state;
        }

        public AstNode getAstNode() {
            return astNode;
        }

        public ParserContext(AstNode astNode) {
            this.astNode = astNode;
        }

        public ParserContext(AstNode astNode, int state) {
            this(astNode);
            this.state = state;
        }
    }

    /**
     * @param grammar
     * @param actionTable The actionTable for the grammar, possibly computed using {@link ActionTable.LALR1Builder#build(Grammar, Rule)}
     */
    public LRParser(Grammar grammar, Rule targetRule, ActionTable actionTable) {
        this.grammar = grammar;
        this.targetRule = targetRule;
        this.actionTable = actionTable;
    }

    public void parse(String txt) {
        parse(getDefaultLexer().iterator(txt));
    }

    public void parse(Reader reader) {
        parse(getDefaultLexer().iterator(reader));
    }

    public AstNode parse(Iterator<Token> it) {

        getLog().debug("\n" + actionTable.toString());

        final PushbackIterator<Token> tokenIterator;
        if (it instanceof PushbackIterator) {
            tokenIterator = (PushbackIterator<Token>) it;
        } else {
            tokenIterator = Iterators.pushbackIterator(it);
        }

        Stack<ParserContext> stack = new Stack<>();

        //start with the initial state
        stack.push(new ParserContext(new AstNodeNonLeaf(targetRule), 0));

        //repeat until done
        boolean completed = false;
        while (!completed) {

            ParserContext currentContext = stack.peek();
            int currentState = currentContext.getState();
            Token token = tokenIterator.next();

            if (getLog().isDebugEnabled()) {
                getLog().debug("-> Current state : " + currentState);
                String msg = "   Input token : " + token.getTokenType();
                String txt = token.getMatchedText();
                if (txt != null) {
                    msg += " (matched text : '" + token.getMatchedText() + "' )";
                }

                getLog().debug(msg);
            }

            Action decision = actionTable.getAction(currentState, token.getTokenType());

            if (decision == null) {
                Set<Lexeme> expected = actionTable.getExpectedLexemes(currentState);
                //if ε is was a possible 'symbol'
                if (expected.contains(Grammar.Empty)) {
                    decision = actionTable.getAction(currentState, Grammar.Empty);
                    tokenIterator.pushback(token);
                } else {
                    throw new ParseError(token, actionTable.getExpectedLexemes(currentState));
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
                    shift(token, stack, decision.getActionParameter());
                    break;
                case Reduce:
                    reduce(stack, decision.getActionParameter());
                    tokenIterator.pushback(token);
                    break;
                default:
                    throw new IllegalStateException(String.format("Illegal action type '%s' !", decision.getActionType().name()));
            }
        }
        return stack.pop().getAstNode();
    }

    /**
     * The new state is added to the stack and becomes the current state
     */
    protected void shift(Token token, final Stack<ParserContext> stack, final int nextState) {
        //add a node that represents the terminal
        stack.add(new ParserContext(new AstNode() {

            @Override
            public List<AstNode> getChildren() {
                return Collections.emptyList();
            }

            @Override
            public Token asToken() {
                return token;
            }

            @Override
            public String toString() {
                return token.toString();
            }
        }, nextState));
    }

    private static class AstNodeNonLeaf implements AstNode {

        private Rule rule;

        private List<AstNode> children = new ArrayList<>();

        public AstNodeNonLeaf(Rule rule) {
            this.rule = rule;
        }

        public List<AstNode> getChildren() {
            return children;
        }

        @Override
        public Token asToken() {
            if (children.size() == 1) {
                return children.get(0).asToken();
            }
            return null;
        }
    }


    protected void reduce(Stack<ParserContext> stack, int ruleIndex) {

        // for each symbol on the left side of the rule, a state is removed from the stack
        Rule rule = grammar.getRuleById(ruleIndex);
        getLog().debug("      - reducing rule : " + rule);

        AstNodeNonLeaf astNode = new AstNodeNonLeaf(rule);
        ParserContext nextParserContext = new ParserContext(astNode);

        List<AstNode> children = astNode.getChildren();
        for (int i = 0; i < rule.getClause().length; i++) {
            children.add(stack.pop().getAstNode());
        }
        Collections.reverse(children);

        if (((BaseRule) rule).getAction() != null) {
            ((BaseRule) rule).getAction().accept(astNode);
        }

        // depending on the state that is now on the top of stack, and the target of the rule,
        // a new state is searched in the goto table and becomes the current state
        int newState = actionTable.getNextState(stack.peek().getState(), rule.getTarget());
        nextParserContext.setState(newState);

        getLog().debug("      - goto " + newState);

        stack.push(nextParserContext);
    }

    public Grammar getGrammar() {
        return grammar;
    }

    @Override
    public Lexer getDefaultLexer() {
        if (defaultLexer == null) {
            defaultLexer = new Lexer(getGrammar().getTerminals());
        }
        return defaultLexer;
    }

    public void setDefaultLexer(Lexer defaultLexer) {
        this.defaultLexer = defaultLexer;
    }
}
