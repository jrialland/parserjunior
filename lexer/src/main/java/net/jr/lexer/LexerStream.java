package net.jr.lexer;

import net.jr.collection.iterators.PushbackIterator;
import net.jr.common.Position;
import net.jr.lexer.impl.Automaton;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LexerStream implements PushbackIterator<Token> {

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

    public LexerStream(Lexer lexer, List<Automaton> automatons, Function<Token, Token> tokenListener, Reader reader) {
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
                    throw new LexicalError(r, lastMatchBegin + lastMatchSize);
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
