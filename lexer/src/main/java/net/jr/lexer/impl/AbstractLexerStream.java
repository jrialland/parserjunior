package net.jr.lexer.impl;

import net.jr.lexer.Lexer;
import net.jr.lexer.LexerStream;
import net.jr.lexer.Token;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AbstractLexerStream implements LexerStream {

    private Lexer lexer;

    private PushbackReader pushbackReader;

    private Function<Token, Token> tokenListener;

    private boolean go = true;

    private LinkedList<Token> buffer = new LinkedList<>();


    public AbstractLexerStream(Lexer lexer, Function<Token, Token> tokenListener, Reader reader) {
        assert lexer != null;
        assert tokenListener != null;
        assert reader != null;

        this.lexer = lexer;
        this.tokenListener = tokenListener;
        this.pushbackReader = reader instanceof PushbackReader ? (PushbackReader) reader : new PushbackReader(reader);

    }

    @Override
    public Lexer getLexer() {
        return lexer;
    }

    @Override
    public void pushback(Token item) {
        buffer.addFirst(tokenListener.apply(item));
    }

    @Override
    public boolean hasNext() {
        if (!go && buffer.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public Token next() {
        if (buffer.isEmpty()) {
            if (go) {
                try {
                    while (buffer.isEmpty()) {
                        go = step(pushbackReader, token -> {
                            buffer.addLast(token);
                        });
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new IllegalStateException();
            }
        }
        return buffer.removeFirst();
    }

    protected Function<Token, Token> getTokenListener() {
        return tokenListener;
    }

    protected abstract boolean step(PushbackReader pushbackReader, Consumer<Token> callback) throws IOException;
}
