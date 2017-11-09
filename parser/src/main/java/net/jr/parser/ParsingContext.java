package net.jr.parser;

import net.jr.lexer.Lexer;
import net.jr.parser.ast.AstNode;

public interface ParsingContext {

    Parser getParser();

    Lexer getLexer();

    AstNode getAstNode();

}
