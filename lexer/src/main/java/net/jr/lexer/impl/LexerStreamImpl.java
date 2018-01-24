package net.jr.lexer.impl;

import net.jr.common.Position;
import net.jr.lexer.*;
import net.jr.lexer.automaton.Automaton;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Basic implementation of lexerStream that recognizes tokens by running in parallel several automatons.
 */
public class LexerStreamImpl extends AbstractLexerStream {

    private List<Automaton> automatons;

    private boolean atStart;

    private Position position = new Position(1, 1);

    private String currentSequence = "";

    /**
     * @param lexer         The lexer that defines this stream
     * @param automatons    automatons to use for recognition
     * @param tokenListener the listener that receives generated tokens (must not be null)
     * @param reader        the input reader
     */
    public LexerStreamImpl(Lexer lexer, List<Automaton> automatons, Function<Token, Token> tokenListener, Reader reader) {
        super(lexer, tokenListener, reader);

        assert automatons != null;
        assert !automatons.isEmpty();

        this.automatons = automatons;

        resetAutomatons();
    }


    @Override
    protected boolean step(PushbackReader reader, Consumer<Token> callback) throws IOException {
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

            if (!getLexer().isFilteredOut(eof)) {
                Token eofToken = getTokenListener().apply(new Token(Lexemes.eof(), position.nextColumn(), ""));
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
                Collections.sort(bestMatches, Comparator.comparingInt(a -> -1 * getLexer().getPriority(a.getTokenType())));
        }

        return bestMatches.get(0);
    }

    private void emitForAutomaton(Automaton a, Consumer<Token> emitCallback) {
        String matchedText = currentSequence.substring(0, a.getMatchedLength());
        currentSequence = "";
        Position p = updatePosition(matchedText);
        if (!getLexer().isFilteredOut(a.getTokenType())) {
            Token token = getTokenListener().apply(new Token(a.getTokenType(), p, matchedText));
            if (token != null) {
                emitCallback.accept(token);
            }
        }
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
