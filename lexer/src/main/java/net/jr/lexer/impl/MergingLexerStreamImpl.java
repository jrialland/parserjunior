package net.jr.lexer.impl;

import net.jr.common.Position;
import net.jr.lexer.*;
import net.jr.lexer.automaton.Automaton;
import net.jr.lexer.automaton.State;
import net.jr.lexer.automaton.Transition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Alternative algorithm for token recognition that involves creating a 'big' automaton, that can have several
 * active states at the same time.
 */
public class MergingLexerStreamImpl extends AbstractLexerStream {

    private Position startPosition, position;

    private StateImpl initial;

    private Set<State<Character>> activeStates = new HashSet<>();

    private Token candidate;

    private StringWriter matched = new StringWriter();

    private void reAssignIds(State<Character> initial) {
        AtomicInteger idCounter = new AtomicInteger(0);
        StatesVisitor.visit(initial, (state) -> {
            int id = idCounter.getAndIncrement();
            if (state.isFinalState()) {
                id = -1 * id;
            }
            state.setId(id);
        });
    }

    public MergingLexerStreamImpl(Lexer lexer, List<Automaton> automatons, Function<Token, Token> tokenListener, Reader reader) {
        super(lexer, tokenListener, reader);

        initial = new StateImpl(0);
        for (Automaton a : automatons) {
            State s = a.getInitialState();
            initial.setFallbackTransition(s.getFallbackTransition());
            if (s != null) {
                initial.getOutgoingTransitions().addAll(s.getOutgoingTransitions());
                if (s.isFinalState()) {
                    initial.setTerminal(s.getTerminal());
                }
            }
        }

        reAssignIds(initial);

        activeStates.add(initial);
        startPosition = Position.start();
        position = startPosition;
    }

    private void emitToken(Consumer<Token> callback, Token token) {
        if (!getLexer().isFilteredOut(token.getTokenType())) {
            TokenListener tokenListener = getLexer().getTokenListener();
            if (tokenListener != null) {
                token = tokenListener.onNewToken(token);
            }
            callback.accept(token);
        }
    }

    protected void emit(PushbackReader reader, int c, Consumer<Token> callback) throws IOException {
        emitToken(callback, candidate);
        candidate = null;
        matched = new StringWriter();
        activeStates.clear();
        activeStates.add(initial);
        reader.unread(c);
        startPosition = position;
    }

    protected void emitEof(Consumer<Token> callback) {
        Token eof = new Token(Lexemes.eof(), position, "");
        emitToken(callback, eof);
    }

    @Override
    protected boolean step(PushbackReader pushbackReader, Consumer<Token> callback) throws IOException {
        int c = pushbackReader.read();
        boolean shouldPushback = false;
        if (c == -1) {
            if (candidate == null) {

                if (activeStates.size() == 1) {

                    State<Character> active = activeStates.iterator().next();
                    if (active.getFallbackTransition() != null && active.getFallbackTransition().getNextState().isFinalState()) {
                        State<Character> s = active.getFallbackTransition().getNextState();
                        candidate = new Token(s.getTerminal(), startPosition, matched.toString());
                    }
                }
            }

            if(candidate == null) {
                if(activeStates.iterator().next() != initial) {
                    throw new LexicalError(c, position);
                }
            } else if(!candidate.getText().isEmpty()){
                emit(pushbackReader, c, callback);
            }
            emitEof(callback);
            return false;
        }

        //apply new states
        Set<State<Character>> newStates = new HashSet<>();
        for (State<Character> state : activeStates) {
            for (Transition<Character> t : state.getOutgoingTransitions()) {
                if (t.isValid((char) c)) {
                    newStates.add(t.getNextState());
                }
            }
        }

        //nothing matches -> see if there are any 'fallback' transition on active nodes
        if (newStates.isEmpty()) {

            List<Transition> fallbacks = activeStates.stream()
                    .map(s -> s.getFallbackTransition())
                    .filter(t -> t != null)
                    .collect(Collectors.toList());

            for (Transition t : fallbacks) {
                newStates.add(t.getNextState());
                shouldPushback = true;
            }
        }

        //update matched text
        if (shouldPushback) {
            pushbackReader.unread(c);
        } else {
            matched.append((char) c);
        }

        //still no transition !
        if (newStates.isEmpty()) {

            if (candidate == null) {
                throw new LexicalError(c, position);
            } else {
                emit(pushbackReader, c, callback);
                return true;
            }
        }

        //find the state that is final with the highest priority
        Optional<State<Character>> finalState = newStates.stream()
                .filter(s -> s.isFinalState())
                .sorted(Comparator.comparingInt(s -> -s.getTerminal().getPriority()))
                .limit(1)
                .findFirst();

        if (finalState.isPresent()) {
            candidate = new Token(finalState.get().getTerminal(), startPosition, matched.toString());
        }

        this.position = this.position.updated((char) c);
        this.activeStates = newStates;
        return true;
    }

    class StateImpl implements State<Character> {

        private Set<Transition<Character>> outgoingTransitions = new HashSet<>();

        private Terminal terminal;

        private int id;

        private Transition<Character> fallbackTransition;

        @Override
        public void setId(int id) {
            this.id = id;
        }

        @Override
        public int getId() {
            return id;
        }

        public StateImpl(int id) {
            setId(id);
        }

        @Override
        public Set<Transition<Character>> getOutgoingTransitions() {
            return outgoingTransitions;
        }

        @Override
        public boolean isFinalState() {
            return terminal != null;
        }

        @Override
        public Terminal getTerminal() {
            return terminal;
        }

        public void setTerminal(Terminal terminal) {
            this.terminal = terminal;
        }

        public void setFallbackTransition(Transition<Character> fallbackTransition) {
            this.fallbackTransition = fallbackTransition;
        }

        @Override
        public Transition<Character> getFallbackTransition() {
            return fallbackTransition;
        }
    }

    public State<Character> getInitialState() {
        return initial;
    }
}
