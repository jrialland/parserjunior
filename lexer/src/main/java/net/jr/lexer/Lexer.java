package net.jr.lexer;

import net.jr.collection.iterators.PushbackIterator;
import net.jr.common.Position;
import net.jr.lexer.impl.Automaton;
import net.jr.lexer.impl.LexemeImpl;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

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

        Automaton added = ((LexemeImpl) tokenType).getAutomaton();
        automatons.add(added);
        filteredOut.add(tokenType);
        return this;
    }

    public List<Token> tokenize(String txt) {
        try {
            return tokenize(new StringReader(txt));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Token> tokenize(Reader reader) throws IOException {
        resetPosition();
        resetAutomatons();
        final List<Token> tokens = new ArrayList<>();
        PushbackReader pReader = reader instanceof PushbackReader ? (PushbackReader) reader : new PushbackReader(reader);
        while (step(pReader, t -> tokens.add(t))) ;
        return tokens;
    }

    public Iterator<Token> iterator(String txt) {
        return iterator(new StringReader(txt));
    }

    public Iterator<Token> iterator(final Reader reader) {
        resetPosition();
        resetAutomatons();
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

    private void resetAutomatons() {
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
                List<Automaton> matchCandidates = automatons.stream().filter(a -> a.isInFinalState()).collect(Collectors.toList());
                Automaton bestMatch = findBest(matchCandidates);
                if (bestMatch != null) {
                    emitForAutomaton(bestMatch, callback);
                } else {
                    throw new LexicalError(r, lastMatchBegin + lastMatchSize);
                }
            }

            Lexeme eof = Lexemes.eof();
            if (!filteredOut.contains(eof)) {
                Token eofToken = new Token(Lexemes.eof(), position.nextColumn(), null);
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
                Automaton bestMatch = findBest(matchCandidates);
                if (bestMatch != null) {
                    emitForAutomaton(bestMatch, callback);
                    reader.unread(r);
                    resetAutomatons();
                } else {
                    throw new LexicalError(r, lastMatchBegin + lastMatchSize);
                }
            }
        }
        return true;
    }

    private Automaton findBest(Collection<Automaton> matchCandidates) {
        // 1 -find the ones with the best matched length
        int l, bestLength = Integer.MIN_VALUE;
        List<Automaton> bestMatches = new ArrayList<>();
        for (Automaton a : matchCandidates) {
            if (a.getMatchedLength() == bestLength) {
                bestMatches.add(a);
            } else if ((l = a.getMatchedLength()) > bestLength) {
                bestLength = l;
                bestMatches.clear();
                bestMatches.add(a);
            }
        }

        switch (bestMatches.size()) {
            case 0:
                return null;
            case 1:
                break;
            default:
                Collections.sort(bestMatches, Comparator.comparingInt(a -> -1 * a.getTokenType().getPriority()));
        }

        return bestMatches.get(0);
    }

    private void emitForAutomaton(Automaton a, EmitCallback emitCallback) {
        String matchedText = currentSequence.substring(0, a.getMatchedLength());
        currentSequence = "";
        lastMatchBegin += lastMatchSize;
        Position p = updatePosition(matchedText);
        if (!filteredOut.contains(a.getTokenType())) {
            Token token = new Token(a.getTokenType(), p, matchedText);
            if (tokenListener != null) {
                tokenListener.onToken(token);
            }
            emitCallback.emit(token);
        }
        lastMatchSize = matchedText.length();
    }

    private Position position;

    private void resetPosition() {
        position = new Position(1, 1);
    }

    private Position updatePosition(String matchedText) {
        Position oldPos = position;
        if(matchedText != null) {
            for (char c : matchedText.toCharArray()) {
                if (c == '\n') {
                    position = position.nextLine();
                } else {
                    position = position.nextColumn();
                }
            }
        }
        return oldPos;
    }
}
