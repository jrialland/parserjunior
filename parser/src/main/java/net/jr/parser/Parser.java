package net.jr.parser;

import net.jr.lexer.Lexer;
import net.jr.lexer.Token;
import net.jr.parser.ast.AstNode;

import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;

public interface Parser {

    AstNode parse(Lexer lexer, Reader reader);

    Lexer getDefaultLexer();

    default AstNode parse(String txt) {
        return parse(getDefaultLexer(), new StringReader(txt));
    }

    default AstNode parse(Reader reader) {
        return parse(getDefaultLexer(), reader);
    }
}
