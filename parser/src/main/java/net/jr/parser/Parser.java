package net.jr.parser;

import net.jr.lexer.Lexer;
import net.jr.lexer.Token;
import net.jr.parser.ast.AstNode;

import java.io.Reader;
import java.util.Iterator;

public interface Parser {

    AstNode parse(Iterator<Token> tokenIterator);

    Lexer getDefaultLexer();

    default AstNode parse(String txt) {
        return parse(getDefaultLexer().iterator(txt));
    }

    default AstNode parse(Reader reader) {
        return parse(getDefaultLexer().iterator(reader));
    }
}
