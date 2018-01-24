package net.jr.lexer.impl;

import net.jr.lexer.Lexer;
import net.jr.lexer.Token;
import net.jr.lexer.automaton.Automaton;

import java.io.PushbackReader;
import java.io.Reader;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Alternative algorithm for token recognition that involves creating a 'big' automaton, that can have several
 * active states at the same time.
 */
public class MergingLexerStreamImpl extends AbstractLexerStream {

    public MergingLexerStreamImpl(Lexer lexer, List<Automaton> automatons, Function<Token, Token> tokenListener, Reader reader) {
        super(lexer, tokenListener, reader);
    }

    @Override
    protected boolean step(PushbackReader pushbackReader, Consumer<Token> callback) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
