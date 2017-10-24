package net.jr.parser;

import net.jr.lexer.Token;

import java.util.Iterator;

public interface Parser {

    void parse(Iterator<Token> tokenIterator);

}
