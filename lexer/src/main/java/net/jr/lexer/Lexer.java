package net.jr.lexer;

import net.jr.common.Symbol;
import net.jr.lexer.impl.Automaton;
import net.jr.lexer.impl.LexemeImpl;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;
import java.util.function.Function;

public class Lexer {

    private List<Automaton> automatons;

    private Set<Lexeme> filteredOut = new HashSet<>();

    private TokenListener tokenListener;

    private Map<Symbol, Integer> priorities = new HashMap<>();

    public static <L extends Symbol> Lexer forLexemes(L... tokenTypes) {
        return new Lexer(Arrays.asList(tokenTypes));
    }

    public static Lexer forLexemes(Collection<? extends Symbol> tokenTypes) {
        return new Lexer(tokenTypes);
    }

    private <L extends Symbol> Lexer(Collection<L> tokenTypes) {
        automatons = new ArrayList<>(tokenTypes.size());
        for (Symbol tokenType : tokenTypes) {
            if (!tokenType.equals(Lexemes.eof())) {
                Automaton a = ((LexemeImpl) tokenType).getAutomaton();
                automatons.add(a);
            }
        }
        priorities = new HashMap<>();
        for (Automaton a : automatons) {
            priorities.put(a.getTokenType(), a.getTokenType().getPriority());
        }
    }

    /**
     * The higher the number, the lower the priority is
     *
     * @param s
     * @param priority 0 is best, Integer.MAX_VALUE is lowest priority
     */
    public void setPriority(Symbol s, int priority) {
        priorities.put(s, priority);
    }

    public int getPriority(Symbol s) {
        Integer p = priorities.get(s);
        return p == null ? 0 : p;
    }

    public void setTokenListener(TokenListener tokenListener) {
        this.tokenListener = tokenListener;
    }

    public TokenListener getTokenListener() {
        return tokenListener;
    }

    public void setFilteredOut(Lexeme tokenType) {

        if (tokenType == null) {
            throw new IllegalArgumentException();
        }

        if (tokenType.equals(Lexemes.eof())) {
            filteredOut.add(tokenType);
            return;
        }

        for (Automaton a : automatons) {
            if (a.getTokenType().equals(tokenType)) {
                filteredOut.add(tokenType);
                return;
            }
        }

        Automaton added = ((LexemeImpl) tokenType).getAutomaton();
        automatons.add(added);
        filteredOut.add(tokenType);
    }

    public boolean isFilteredOut(Lexeme tokenType) {
        return filteredOut.contains(tokenType);
    }

    public List<Token> tokenize(String txt) {
        try {
            return tokenize(new StringReader(txt));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Token> tokenize(Reader reader) throws IOException {
        final List<Token> tokens = new ArrayList<>();
        Iterator<Token> it = iterator(reader);
        while (it.hasNext()) {
            tokens.add(it.next());
        }
        return tokens;
    }

    public LexerStream iterator(final Reader reader) {
        List<Automaton> clonedAutomatons = new ArrayList<>(automatons.size());
        try {
            for (Automaton a : automatons) {
                clonedAutomatons.add((Automaton) a.clone());
            }
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        Function<Token, Token> listener = tokenListener == null ? t -> t : t -> tokenListener.onNewToken(t);
        return new LexerStream(this, clonedAutomatons, listener, reader);
    }

}
