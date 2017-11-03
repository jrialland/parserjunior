package net.jr.parser;

import net.jr.lexer.Lexer;
import net.jr.lexer.Token;
import net.jr.parser.ast.AstNode;

import java.util.Iterator;

public interface Parser {

    AstNode parse(Iterator<Token> tokenIterator);

    Lexer getDefaultLexer();
}
