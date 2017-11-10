package net.jr.parser;

import net.jr.lexer.LexerStream;
import net.jr.parser.ast.AstNode;

/**
 * describes the state of the ongoing parsing.
 */
public interface ParsingContext {

    Parser getParser();

    LexerStream getLexerStream();

    AstNode getAstNode();

}
