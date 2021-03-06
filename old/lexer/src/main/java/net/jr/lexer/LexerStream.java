package net.jr.lexer;

import net.jr.collection.iterators.PushbackIterator;

public interface LexerStream extends PushbackIterator<Token> {

    Lexer getLexer();

    @Override
    void pushback(Token item);

    @Override
    boolean hasNext();

    @Override
    Token next();
}
