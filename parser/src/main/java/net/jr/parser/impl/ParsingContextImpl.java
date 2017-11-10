package net.jr.parser.impl;

import net.jr.lexer.LexerStream;
import net.jr.parser.Parser;
import net.jr.parser.ParsingContext;
import net.jr.parser.ast.AstNode;

class ParsingContextImpl implements ParsingContext {

    private Parser parser;

    private LexerStream lexerStream;

    private AstNode astNode;

    public ParsingContextImpl(Parser parser, LexerStream lexerStream, AstNode astNode) {
        this.parser = parser;
        this.lexerStream = lexerStream;
        this.astNode = astNode;
    }

    @Override
    public Parser getParser() {
        return parser;
    }

    public void setParser(Parser parser) {
        this.parser = parser;
    }

    @Override
    public AstNode getAstNode() {
        return astNode;
    }

    @Override
    public LexerStream getLexerStream() {
        return lexerStream;
    }
}
