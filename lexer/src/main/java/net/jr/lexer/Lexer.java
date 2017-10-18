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

    private void reset() {
        automatons.forEach(Automaton::reset);
        atStart = true;
    }

    private void emit(Token token) {
        if (!(tokenListener == null || filteredOut.contains(token.getTokenType()))) {
            tokenListener.onToken(token);
        }
    }

    private void emit(Automaton a) {
        String matchedText = currentSequence.substring(0, a.getMatchedLength());
        currentSequence = "";
        lastMatchBegin += lastMatchSize;
        lastMatchSize = matchedText.length();
        Token token = new Token(a.getTokenType(), lastMatchBegin, matchedText);
        emit(token);
    }

    private boolean step(PushbackReader reader) throws IOException {
        int r = reader.read();
        if (r == -1) { //eof reached
            if (!atStart) {
                Optional<Automaton> bestMatch = automatons.stream().filter(a -> a.isInFinalState()).max(Comparator.comparingInt(a -> a.getMatchedLength()));
                if (bestMatch.isPresent()) {
                    emit(bestMatch.get());
                } else {
                    throw new LexicalError(lastMatchBegin + lastMatchSize);
                }
            }
            emit(new Token(Lexeme.Eof, lastMatchBegin + lastMatchSize, ""));
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
                    emit(bestMatch.get());
                    reader.unread(r);
                    reset();
                } else {
                    throw new LexicalError(lastMatchBegin + lastMatchSize);
                }
            }
        }
        return true;
    }
}
