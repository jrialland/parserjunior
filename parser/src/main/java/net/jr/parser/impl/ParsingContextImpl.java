package net.jr.parser.impl;

import net.jr.collection.iterators.PushbackIterator;
import net.jr.lexer.Lexer;
import net.jr.lexer.Token;
import net.jr.parser.Parser;
import net.jr.parser.ParsingContext;
import net.jr.parser.ast.AstNode;

public class ParsingContextImpl implements ParsingContext {


    private Parser parser;

    private Lexer lexer;

    private PushbackIterator<Token> tokenIterator;

    private AstNode astNode;

    public ParsingContextImpl(Parser parser, Lexer lexer, PushbackIterator<Token> tokenIterator, AstNode astNode) {
        this.parser = parser;
        this.lexer = lexer;
        this.tokenIterator = tokenIterator;
        this.astNode = astNode;
    }

    @Override
    public Parser getParser() {
        return parser;
    }

    public void setParser(Parser parser) {
        this.parser = parser;
    }

    public PushbackIterator<Token> getTokenIterator() {
        return tokenIterator;
    }

    public void setTokenIterator(PushbackIterator<Token> tokenIterator) {
        this.tokenIterator = tokenIterator;
    }

    @Override
    public AstNode getAstNode() {
        return astNode;
    }

    public void setAstNode(AstNode astNode) {
        this.astNode = astNode;
    }

    public void setLexer(Lexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public Lexer getLexer() {
        return lexer;
    }
}
