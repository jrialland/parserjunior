package net.jr.lexer;

import net.jr.common.Symbol;
import net.jr.lexer.automaton.Automaton;
import net.jr.lexer.impl.MergingLexerStreamImpl;
import net.jr.lexer.impl.TerminalImpl;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * {@link Lexer} reference all the valid Terminal that can be scanned, and can produce tokens by analyzing input text, searching
 * for matches in the list of the referenced Lexemes.
 */
public class Lexer {

    private List<Automaton> automatons;

    private Set<Terminal> filteredOut = new HashSet<>();

    private TokenListener tokenListener = t -> t;

    private int maxId = 0;

    private <L extends Symbol> Lexer(Collection<L> tokenTypes) {
        automatons = new ArrayList<>(tokenTypes.size());
        for (Symbol tokenType : tokenTypes) {

            if (!tokenType.isTerminal()) {
                throw new IllegalArgumentException("Not a terminal : " + tokenType);
            }

            if (!tokenType.equals(Lexemes.eof())) {
                tokenType.setId(maxId++);
                Automaton a = ((TerminalImpl) tokenType).getAutomaton();
                automatons.add(a);
            }
        }

    }

    public List<Terminal> getTokenTypes() {
        return automatons.stream().map(a -> a.getTokenType()).collect(Collectors.toList());
    }

    /**
     * Creates a new {@link Lexer} that can recognize the passed Lexemes
     *
     * @param tokenTypes
     * @param <L>
     * @return
     */
    @SafeVarargs
    public static <L extends Symbol> Lexer forLexemes(L... tokenTypes) {
        List<Symbol> list = new ArrayList<>(tokenTypes.length);
        for (L tokenType : tokenTypes) {
            list.add(tokenType);
        }
        return new Lexer(list);
    }

    /**
     * Creates a new {@link Lexer} that can recognize the passed Lexemes
     *
     * @param tokenTypes
     * @return
     */
    public static <L extends Symbol> Lexer forLexemes(Collection<L> tokenTypes) {
        return new Lexer(tokenTypes);
    }

    /**
     * gets the associated {@link TokenListener}.
     * <p>
     * may return null
     *
     * @return
     */
    public TokenListener getTokenListener() {
        return tokenListener;
    }

    /**
     * associates a {@link TokenListener} with this {@link Lexer}
     *
     * @param tokenListener
     */
    public void setTokenListener(TokenListener tokenListener) {
        this.tokenListener = tokenListener;
    }

    /**
     * Adds a token type to the list of the 'filtered out' tokens, I.e. the tokens type that are recognized, but not put in the list of recognized tokens.
     *
     * @param tokenType
     */
    public void setFilteredOut(Terminal tokenType) {

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

        tokenType.setId(maxId++);
        Automaton added = ((TerminalImpl) tokenType).getAutomaton();
        automatons.add(added);
        filteredOut.add(tokenType);
    }

    /**
     * whether a particular token type is filtered out or not
     *
     * @param tokenType
     * @return
     */
    public boolean isFilteredOut(Terminal tokenType) {
        return filteredOut.contains(tokenType);
    }

    public List<Token> tokenize(String txt) {
        try {
            return tokenize(new StringReader(txt));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * reads some text, put the recognized tokens in a list
     *
     * @param reader
     * @return the recognized tokens
     * @throws IOException due to io operations on reader
     */
    public List<Token> tokenize(Reader reader) throws IOException {
        final List<Token> tokens = new ArrayList<>();
        Iterator<Token> it = iterator(reader);
        while (it.hasNext()) {
            tokens.add(it.next());
        }
        return tokens;
    }

    /**
     * builds a {@link LexerStream} out this Lexer
     *
     * @param reader
     * @return
     */
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
        return new MergingLexerStreamImpl(this, clonedAutomatons, listener, reader);
    }

}
