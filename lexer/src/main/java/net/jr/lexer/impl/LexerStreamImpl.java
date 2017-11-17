package net.jr.lexer.impl;

import net.jr.common.Position;
import net.jr.lexer.*;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Actual lexing is done by this class.
 * instances are obtained by calling {@link Lexer#iterator(Reader)}
 */
public class LexerStreamImpl implements LexerStream {

    private Lexer lexer;

    private List<Automaton> automatons;

    private boolean atStart;

    private Position position = new Position(1, 1);

    private PushbackReader pushbackReader;

    private String currentSequence = "";

    private int lastMatchBegin = 0, lastMatchSize = 0;

    private Function<Token, Token> tokenListener;

    private boolean go = true;

    private LinkedList<Token> buffer = new LinkedList<>();

    /**
     * @param lexer         The lexer that defines this stream
     * @param automatons    automatons to use for recognition
     * @param tokenListener the listener that receives generated tokens (must not be null)
     * @param reader        the input reader
     */
    public LexerStreamImpl(Lexer lexer, List<Automaton> automatons, Function<Token, Token> tokenListener, Reader reader) {
        assert lexer != null;
        assert tokenListener != null;
        assert reader != null;
        this.lexer = lexer;
        this.automatons = automatons;
        this.tokenListener = tokenListener;
        this.pushbackReader = reader instanceof PushbackReader ? (PushbackReader) reader : new PushbackReader(reader);
        resetAutomatons();
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

    /**
     * Empties the internal buffer
     *
     * @return the former content of the buffer, older tokens first (i.e the token that would have appeared next when calling {@link LexerStream#next()} is the last item in the returned list)
     */
    public List<Token> flushBuffer() {
        List<Token> copy = new ArrayList<>(buffer);
        buffer.clear();
        Collections.reverse(copy);
        return copy;
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

    private boolean step(PushbackReader reader, Consumer<Token> callback) throws IOException {
        int r = reader.read();
        if (r == -1) { //eof reached

            if (!atStart) {
                List<Automaton> matchCandidates = automatons.stream().filter(a -> a.isInFinalState()).collect(Collectors.toList());
                Automaton bestMatch = findBest(matchCandidates);
                if (bestMatch != null) {
                    emitForAutomaton(bestMatch, callback);
                } else {
                    throw new LexicalError(r, position);
                }
            }

            Lexeme eof = Lexemes.eof();

            if (!lexer.isFilteredOut(eof)) {
                Token eofToken = tokenListener.apply(new Token(Lexemes.eof(), position.nextColumn(), ""));
                if (eofToken != null) {
                    callback.accept(eofToken);
                }
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
                    throw new LexicalError(r, position);
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
                Collections.sort(bestMatches, Comparator.comparingInt(a -> -1 * lexer.getPriority(a.getTokenType())));
        }

        return bestMatches.get(0);
    }

    private void emitForAutomaton(Automaton a, Consumer<Token> emitCallback) {
        String matchedText = currentSequence.substring(0, a.getMatchedLength());
        currentSequence = "";
        lastMatchBegin += lastMatchSize;
        Position p = updatePosition(matchedText);
        if (!lexer.isFilteredOut(a.getTokenType())) {
            Token token = tokenListener.apply(new Token(a.getTokenType(), p, matchedText));
            if (token != null) {
                emitCallback.accept(token);
            }
        }
        lastMatchSize = matchedText.length();
    }

    private Position updatePosition(String matchedText) {
        Position oldPos = position;
        if (matchedText != null) {
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

    private void resetAutomatons() {
        automatons.forEach(Automaton::reset);
        atStart = true;
    }

}
