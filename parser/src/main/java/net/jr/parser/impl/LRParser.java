package net.jr.parser.impl;

import net.jr.common.Symbol;
import net.jr.lexer.Lexeme;
import net.jr.lexer.Lexer;
import net.jr.lexer.LexerStream;
import net.jr.lexer.Token;
import net.jr.parser.Grammar;
import net.jr.parser.ParseError;
import net.jr.parser.Parser;
import net.jr.parser.Rule;
import net.jr.parser.ast.AstNode;
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

    private class Context {

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

        public Context(AstNode astNode) {
            this.astNode = astNode;
        }

        public Context(AstNode astNode, int state) {
            this(astNode);
            this.state = state;
        }
    }

    /**
     * @param grammar     target grammar
     * @param targetRule  the target rule for this parser
     * @param actionTable The actionTable for the grammar, possibly computed using {@link ActionTable.LALR1Builder#build(Grammar, Rule)}
     */
    public LRParser(Grammar grammar, Rule targetRule, ActionTable actionTable) {
        this.grammar = grammar;
        this.targetRule = targetRule;
        this.actionTable = actionTable;
    }

    public AstNode parse(Lexer lexer, Reader reader) {
        final LexerStream lexerStream = lexer.iterator(reader);

        Stack<Context> stack = new Stack<>();

        //start with the initial state
        stack.push(new Context(new AstNodeNonLeaf(targetRule), 0));

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
                Set<Lexeme> expected = actionTable.getExpectedLexemes(currentState);
                //if ε is was a possible 'symbol'
                if (expected.contains(Grammar.Empty)) {
                    decision = actionTable.getAction(currentState, Grammar.Empty);
                    lexerStream.pushback(token);
                } else {
                    throw new ParseError(token, actionTable.getExpectedLexemes(currentState));
                }
            }

            getLog().trace(String.format("   Decision : %s %d", decision.getActionType().name(), decision.getActionParameter()));

            switch (decision.getActionType()) {
                case Accept:
                    accept(stack, lexerStream);
                    return stack.pop().getAstNode();
                case Fail:
                    throw new ParseError(token, actionTable.getExpectedLexemes(currentState));
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

    protected void accept(Stack<Context> stack, LexerStream lexerStream) {
        Rule targetRule = grammar.getRulesTargeting(grammar.getTargetSymbol()).iterator().next();
        AstNode node = makeNode(stack, lexerStream, targetRule);
        stack.push(new Context(node));
    }

    /**
     * The new state is added to the stack and becomes the current state
     */
    protected void shift(Token token, final Stack<Context> stack, final int nextState) {
        //add a node that represents the terminal
        stack.add(new Context(new AstNodeLeaf(token), nextState));
    }

    private static class AstNodeLeaf implements AstNode {
        private Token token;

        public AstNodeLeaf(Token token) {
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

        public AstNodeNonLeaf(Rule rule) {
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

    protected AstNode makeNode(Stack<Context> stack, final LexerStream lexerStream, Rule rule) {
        // for each symbol on the left side of the rule, a state is removed from the stack
        getLog().trace("      - reducing rule : " + rule);
        AstNodeNonLeaf astNode = new AstNodeNonLeaf(rule);
        List<AstNode> children = astNode.getChildren();
        for (int i = 0; i < rule.getClause().length; i++) {
            children.add(stack.pop().getAstNode());
        }

        Collections.reverse(children);

        if (((BaseRule) rule).getAction() != null) {
            ParsingContextImpl parsingContext = new ParsingContextImpl(this, lexerStream, astNode);
            ((BaseRule) rule).getAction().accept(parsingContext);
        }

        return astNode;
    }

    protected void reduce(Stack<Context> stack, final LexerStream lexerStream, int ruleIndex) {
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
}
