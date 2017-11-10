package net.jr.parser;

import net.jr.lexer.LexerStream;
import net.jr.parser.ast.AstNode;

public interface ParsingContext {

    Parser getParser();

    LexerStream getLexerStream();

    AstNode getAstNode();

}
