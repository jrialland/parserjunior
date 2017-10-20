package net.jr.lexer;

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
            Automaton a = ((LexemeImpl) tokenType).getAutomaton();
            automatons.add(a);
        }
    }

    public void setTokenListener(TokenListener tokenListener) {
        this.tokenListener = tokenListener;
    }

    public void filterOut(Lexeme tokenType) {
        filteredOut.add(tokenType);
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
        while (step(pReader)) ;
    }

    public Iterator<Token> iterator(Reader reader) {
        reset();
        final PushbackReader pReader = reader instanceof PushbackReader ? (PushbackReader) reader : new PushbackReader(reader);

        return new Iterator<Token>() {

            private boolean go = true;

            private LinkedList<Token> buffer = new LinkedList<>();

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
                            while(buffer.isEmpty()) {
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

    private boolean step(PushbackReader reader) throws IOException {
        return step(reader, token -> {
            if (!(tokenListener == null || filteredOut.contains(token.getTokenType()))) {
                tokenListener.onToken(token);
            }
        });
    }


    private boolean step(PushbackReader reader, EmitCallback callback) throws IOException {
        int r = reader.read();
        if (r == -1) { //eof reached
            if (!atStart) {
                Optional<Automaton> bestMatch = automatons.stream().filter(a -> a.isInFinalState()).max(Comparator.comparingInt(a -> a.getMatchedLength()));
                if (bestMatch.isPresent()) {
                    emitForAutomaton(bestMatch.get(), callback);
                } else {
                    throw new LexicalError(lastMatchBegin + lastMatchSize);
                }
            }
            callback.emit(new Token(Lexemes.eof(), lastMatchBegin + lastMatchSize, ""));
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
                    throw new LexicalError(lastMatchBegin + lastMatchSize);
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
        Token token = new Token(a.getTokenType(), lastMatchBegin, matchedText);
        emitCallback.emit(token);
    }
}
