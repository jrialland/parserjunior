package net.jr.lexer;

import net.jr.collection.iterators.PushbackIterator;

import java.util.List;

public interface LexerStream extends PushbackIterator<Token> {

    Lexer getLexer();

    @Override
    void pushback(Token item);

    @Override
    boolean hasNext();

    @Override
    Token next();
}
