package net.jr.lexer.impl;

import net.jr.common.Position;
import net.jr.lexer.*;
import net.jr.lexer.automaton.Automaton;
import net.jr.lexer.automaton.State;
import net.jr.lexer.automaton.Transition;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Alternative algorithm for token recognition that involves creating a 'big' automaton, that can have several
 * active states at the same time.
 */
public class MergingLexerStreamImpl extends AbstractLexerStream {


    class StateImpl implements State<Character> {

        private Set<Transition<Character>> outgoingTransitions = new HashSet<>();

        private Lexeme lexeme;

        @Override
        public Set<Transition<Character>> getOutgoingTransitions() {
            return outgoingTransitions;
        }

        @Override
        public boolean isFinalState() {
            return lexeme != null;
        }

        @Override
        public Lexeme getLexeme() {
            return lexeme;
        }

        public void setLexeme(Lexeme lexeme) {
            this.lexeme = lexeme;
        }
    }

    private Position startPosition, position;

    private StateImpl initial;

    private Set<State<Character>> activeStates = new HashSet<>();

    private Token candidate;

    private StringWriter sw = new StringWriter();

    public MergingLexerStreamImpl(Lexer lexer, List<Automaton> automatons, Function<Token, Token> tokenListener, Reader reader) {
        super(lexer, tokenListener, reader);
        initial = new StateImpl();
        for(Automaton a : automatons) {
            State s = a.getInitialState();
            initial.getOutgoingTransitions().addAll(s.getOutgoingTransitions());
            if(s.isFinalState()) {
                initial.setLexeme(s.getLexeme());
            }
        }
        activeStates.add(initial);
        startPosition = Position.start();
        position = startPosition;
    }

    protected void emit(PushbackReader reader, int c, Consumer<Token> callback) throws IOException {

        if(!getLexer().isFilteredOut(candidate.getTokenType())){

            if(getLexer().getTokenListener() != null) {
                getLexer().getTokenListener().onNewToken(candidate);
            }

            callback.accept(candidate);
        }
        candidate = null;
        sw = new StringWriter();
        activeStates.clear();
        activeStates.add(initial);
        reader.unread(c);
        startPosition = position;
    }

    @Override
    protected boolean step(PushbackReader pushbackReader, Consumer<Token> callback) throws IOException {
        int c = pushbackReader.read();

        if(c == -1) {
            if(candidate == null) {
                throw new LexicalError(c, position);
            } else {
                emit(pushbackReader, c, callback);
                callback.accept(new Token(Lexemes.eof(), position, ""));
                return false;
            }
        }

        //update matched text
        sw.append((char)c);

        //apply new states
        Set<State<Character>> newStates = new HashSet<>();
        for(State<Character> state : activeStates) {
            for(Transition<Character> t : state.getOutgoingTransitions()) {
                if(t.isValid((char)c)) {
                    newStates.add(t.getNextState());
                }
            }
        }

        //no transition !
        if(newStates.isEmpty()) {

            if(candidate == null) {
                throw new LexicalError(c, position);
            } else {
                emit(pushbackReader, c, callback);
                return true;
            }
        }

        //find the state that is final with the highest priority
        List<State> posssibleFinals = newStates.stream()
                .filter(s -> s.isFinalState())
                .sorted(Comparator.comparingInt(s->-getLexer().getPriority(s.getLexeme()))).collect(Collectors.toList());

        State<Character> finalState = posssibleFinals.isEmpty() ? null : posssibleFinals.get(0);

        if(finalState != null) {
            candidate = new Token(finalState.getLexeme(), startPosition, sw.toString());
        }

        this.position = this.position.updated((char)c);
        this.activeStates = newStates;
        return true;
    }
}
