package net.jr.lexer;

import net.jr.collection.iterators.PushbackIterator;
import net.jr.lexer.impl.Automaton;
import net.jr.lexer.impl.LexemeImpl;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;

public class Lexer {

    private List<Automaton> automatons;

    private boolean atStart;

    private int lastMatchBegin = 0, lastMatchSize = 0;

    private Set<Lexeme> filteredOut = new HashSet<>();

    private TokenListener tokenListener;

    private String currentSequence = "";

    public Lexer(Lexeme... tokenTypes) {
        this(Arrays.asList(tokenTypes));
    }

    public Lexer(Collection<Lexeme> tokenTypes) {
        automatons = new ArrayList<>(tokenTypes.size());
        for (Lexeme tokenType : tokenTypes) {
            if (!tokenType.equals(Lexemes.eof())) {
                Automaton a = ((LexemeImpl) tokenType).getAutomaton();
                automatons.add(a);
            }
        }
    }

    public Lexer tokenListener(TokenListener tokenListener) {
        this.tokenListener = tokenListener;
        return this;
    }

    public Lexer filterOut(Lexeme tokenType) {

        if (tokenType == null) {
            throw new IllegalArgumentException();
        }

        if (tokenType.equals(Lexemes.eof())) {
            filteredOut.add(tokenType);
            return this;
        }

        for (Automaton a : automatons) {
            if (a.getTokenType().equals(tokenType)) {
                filteredOut.add(tokenType);
                return this;
            }
        }

        Automaton added = ((LexemeImpl)tokenType).getAutomaton();
        automatons.add(added);
        filteredOut.add(tokenType);
        return this;
    }

    public void tokenize(String txt) {
        try {
            tokenize(new StringReader(txt));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void tokenize(Reader reader) throws IOException {
        reset();
        PushbackReader pReader = reader instanceof PushbackReader ? (PushbackReader) reader : new PushbackReader(reader);
        while (step(pReader, t -> {
        })) ;
    }

    public Iterator<Token> iterator(final Reader reader) {
        reset();
        PushbackReader pReader = reader instanceof PushbackReader ? (PushbackReader) reader : new PushbackReader(reader);
        return new PushbackIterator<Token>() {

            private boolean go = true;

            private LinkedList<Token> buffer = new LinkedList<>();

            @Override
            public void pushback(Token item) {
                buffer.addFirst(item);
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
                                go = step(pReader, token -> {
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
        };

    }

    private void reset() {
        automatons.forEach(Automaton::reset);
        atStart = true;
    }

    private interface EmitCallback {
        void emit(Token token);
    }

    private boolean step(PushbackReader reader, EmitCallback callback) throws IOException {
        int r = reader.read();
        if (r == -1) { //eof reached

            if (!atStart) {
                Optional<Automaton> bestMatch = automatons.stream().filter(a -> a.isInFinalState()).max(Comparator.comparingInt(a -> a.getMatchedLength()));
                if (bestMatch.isPresent()) {
                    emitForAutomaton(bestMatch.get(), callback);
                } else {
                    throw new LexicalError(r, lastMatchBegin + lastMatchSize);
                }
            }

            Lexeme eof = Lexemes.eof();
            if (!filteredOut.contains(eof)) {
                Token eofToken = new Token(Lexemes.eof(), lastMatchBegin + lastMatchSize, "");
                if (tokenListener != null) {
                    tokenListener.onToken(eofToken);
                }
                callback.emit(eofToken);
            }

            return false;
        } else {
            final char c = (char) r;
            currentSequence += c;
            List<Automaton> matchCandidates = new ArrayList<>();
            boolean hasUp = false;
            for (Automaton a : automatons) {
                boolean wasInFinalState = a.isInFinalState();
                boolean dead = a.step(c);
                if (dead) {
                    if (wasInFinalState) {
                        matchCandidates.add(a);
                    }
                } else {
                    hasUp = true;
                }
            }
            atStart = false;
            if (!hasUp) {
                Optional<Automaton> bestMatch = matchCandidates.stream().max(Comparator.comparingInt(Automaton::getMatchedLength));
                if (bestMatch.isPresent()) {
                    emitForAutomaton(bestMatch.get(), callback);
                    reader.unread(r);
                    reset();
                } else {
                    throw new LexicalError(r, lastMatchBegin + lastMatchSize);
                }
            }
        }
        return true;
    }

    private void emitForAutomaton(Automaton a, EmitCallback emitCallback) {
        String matchedText = currentSequence.substring(0, a.getMatchedLength());
        currentSequence = "";
        lastMatchBegin += lastMatchSize;
        lastMatchSize = matchedText.length();

        if (!filteredOut.contains(a.getTokenType())) {
            Token token = new Token(a.getTokenType(), lastMatchBegin, matchedText);
            if (tokenListener != null) {
                tokenListener.onToken(token);
            }
            emitCallback.emit(token);
        }
    }
}
