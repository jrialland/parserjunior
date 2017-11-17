package net.jr.parser.impl;

import net.jr.common.Symbol;
import net.jr.lexer.*;
import net.jr.parser.*;
import net.jr.parser.ast.AstNode;
import net.jr.parser.ast.AstNodeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.util.*;

/**
 * Implementation of the LR parser algorithm.
 */
public class LRParser implements Parser {

    private static final Logger LOGGER = LoggerFactory.getLogger(LRParser.class);

    private static Logger getLog() {
        return LOGGER;
    }

    private Grammar grammar;

    private Lexer defaultLexer;

    private ActionTable actionTable;

    private AstNodeFactory astNodeFactory = new DefaultAstNodeFactory();

    private ParserListener parserListener;

    private class Context {

        private int state;

        private AstNode astNode;

        void setState(int state) {
            this.state = state;
        }

        int getState() {
            return state;
        }

        AstNode getAstNode() {
            return astNode;
        }

        Context(AstNode astNode) {
            this.astNode = astNode;
        }

        public Context(AstNode astNode, int state) {
            this(astNode);
            this.state = state;
        }
    }

    /**
     * @param grammar     target grammar
     * @param actionTable The actionTable for the grammar, possibly computed using {@link ActionTable.LALR1Builder#build(Grammar)}
     */
    public LRParser(Grammar grammar, ActionTable actionTable) {
        this.grammar = grammar;
        this.actionTable = actionTable;
    }

    public AstNode parse(Lexer lexer, Reader reader) {

        final LexerStream lexerStream = lexer.iterator(reader);
        Stack<Context> stack = new Stack<>();

        //start with the initial state
        Rule targetRule = grammar.getRulesTargeting(grammar.getTargetSymbol()).iterator().next();
        stack.push(new Context(astNodeFactory.newNonLeafNode(targetRule), 0));

        //repeat until done
        while (true) {

            Context currentContext = stack.peek();
            int currentState = currentContext.getState();
            Token token = lexerStream.next();

            if (getLog().isTraceEnabled()) {
                getLog().trace("-> Current state : " + currentState);
                String msg = "   Input token : " + token.getTokenType();
                String txt = token.getText();
                if (txt != null) {
                    msg += " (matched text : '" + token.getText() + "' )";
                }

                getLog().trace(msg);
            }

            Action decision = actionTable.getAction(currentState, token.getTokenType());

            if (decision == null) {
                Set<Symbol> expected = actionTable.getExpectedTerminals(currentState);
                //if ε is part of the expected symbols
                if (expected.contains(Lexemes.empty())) {
                    decision = actionTable.getAction(currentState, Lexemes.empty());
                    lexerStream.pushback(token);
                } else {
                    throw new ParseError(token, actionTable.getExpectedTerminals(currentState));
                }
            }

            getLog().trace(String.format("   Decision : %s %d", decision.getActionType().name(), decision.getActionParameter()));

            switch (decision.getActionType()) {
                case Accept:
                    accept(stack, lexerStream);
                    return stack.pop().getAstNode();
                case Fail:
                    fail(token, lexerStream, currentContext);
                    break;
                case Shift:
                    shift(token, stack, decision.getActionParameter());
                    break;
                case Reduce:
                    reduce(stack, lexerStream, decision.getActionParameter());
                    lexerStream.pushback(token);
                    break;
                default:
                    throw new IllegalStateException(String.format("Illegal action type '%s' !", decision.getActionType().name()));
            }
        }
    }

    private void fail(Token token, LexerStream lexerStream, Context context) {
        ParseError parseError = new ParseError(token, actionTable.getExpectedTerminals(context.getState()));
        if(parserListener != null) {
            parserListener.onParseError(parseError, new ParsingContextImpl(this, lexerStream, context.getAstNode()));
        } else {
            throw parseError;
        }
    }

    private void accept(Stack<Context> stack, LexerStream lexerStream) {
        Rule targetRule = grammar.getRulesTargeting(grammar.getTargetSymbol()).iterator().next();
        AstNode node = makeNode(stack, lexerStream, targetRule);
        stack.push(new Context(node));
    }

    /**
     * The new state is added to the stack and becomes the current state
     */
    private void shift(Token token, final Stack<Context> stack, final int nextState) {
        //add a node that represents the terminal
        stack.add(new Context(astNodeFactory.newLeafNode(token), nextState));
    }

    private static class AstNodeLeaf implements AstNode {
        private Token token;

        AstNodeLeaf(Token token) {
            this.token = token;
        }

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

        @Override
        public Symbol getSymbol() {
            return asToken().getTokenType();
        }

        @Override
        public String repr() {
            return token.getText();
        }
    }

    private static class AstNodeNonLeaf implements AstNode {

        private Rule rule;

        private List<AstNode> children = new ArrayList<>();

        AstNodeNonLeaf(Rule rule) {
            this.rule = rule;
        }

        public List<AstNode> getChildren() {
            return children;
        }

        @Override
        public Symbol getSymbol() {
            return rule.getTarget();
        }

        @Override
        public Token asToken() {
            if (children.size() == 1) {
                return children.get(0).asToken();
            }
            return null;
        }

        @Override
        public String toString() {
            return rule.getTarget().toString();
        }

    }

    private static class DefaultAstNodeFactory implements AstNodeFactory {

        @Override
        public AstNode newLeafNode(Token token) {
            return new AstNodeLeaf(token);
        }

        @Override
        public AstNode newNonLeafNode(Rule rule) {
            return new AstNodeNonLeaf(rule);
        }
    }

    private static boolean isEofNode(AstNode astNode) {
        Token token = astNode.asToken();
        return token != null && token.getTokenType().equals(Lexemes.eof());
    }

    private AstNode makeNode(Stack<Context> stack, final LexerStream lexerStream, Rule rule) {
        // for each symbol on the left side of the rule, a state is removed from the stack
        getLog().trace("      - reducing rule : " + rule);
        AstNode astNode = astNodeFactory.newNonLeafNode(rule);
        List<AstNode> children = astNode.getChildren();
        for (int i = 0; i < rule.getClause().length; i++) {
            AstNode popped = stack.pop().getAstNode();
            if(!isEofNode(popped)) {
                children.add(popped);
            }
        }

        Collections.reverse(children);

        ParsingContextImpl parsingContext = new ParsingContextImpl(this, lexerStream, astNode);
        if (((BaseRule) rule).getAction() != null) {
            ((BaseRule) rule).getAction().accept(parsingContext);
        }
        if (parserListener != null) {
            parserListener.onReduce(rule, parsingContext);
        }

        return astNode;
    }

    private void reduce(Stack<Context> stack, final LexerStream lexerStream, int ruleIndex) {
        Rule rule = grammar.getRuleById(ruleIndex);
        AstNode astNode = makeNode(stack, lexerStream, rule);
        Context nextParserContext = new Context(astNode);
        // depending on the state that is now on the top of stack, and the target of the rule,
        // a new state is searched in the goto table and becomes the current state
        int newState = actionTable.getNextState(stack.peek().getState(), rule.getTarget());
        nextParserContext.setState(newState);
        getLog().trace("      - goto " + newState);
        stack.push(nextParserContext);
    }

    public Grammar getGrammar() {
        return grammar;
    }

    @Override
    public Lexer getDefaultLexer() {
        if (defaultLexer == null) {
            defaultLexer = Lexer.forLexemes(getGrammar().getTerminals());
        }
        return defaultLexer;
    }

    public void setDefaultLexer(Lexer defaultLexer) {
        this.defaultLexer = defaultLexer;
    }


    @Override
    public void setParserListener(ParserListener parserListener) {
        this.parserListener = parserListener;
    }

    @Override
    public ParserListener getParserListener() {
        return parserListener;
    }

    @Override
    public void setAstNodeFactory(AstNodeFactory astNodeFactory) {
        this.astNodeFactory = astNodeFactory;
    }

    @Override
    public AstNodeFactory getAstNodeFactory() {
        return astNodeFactory;
    }
}
