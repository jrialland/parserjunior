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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Alternative algorithm for token recognition that involves creating a 'big' automaton, that can have several
 * active states at the same time.
 */
public class MergingLexerStreamImpl extends AbstractLexerStream {

    private static final Logger LOGGER = LoggerFactory.getLogger(MergingLexerStreamImpl.class);
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

    protected void emit(PushbackReader reader, int c, Consumer<Token> callback) throws IOException {

        if (!getLexer().isFilteredOut(candidate.getTokenType())) {

            if (getLexer().getTokenListener() != null) {
                candidate = getLexer().getTokenListener().onNewToken(candidate);
            }

            callback.accept(candidate);
        }
        candidate = null;
        matched = new StringWriter();
        activeStates.clear();
        activeStates.add(initial);
        reader.unread(c);
        startPosition = position;
    }

    protected void emitEof(Consumer<Token> callback) {
        Token eof = new Token(Lexemes.eof(), position, "");
        if (!getLexer().isFilteredOut(eof.getTokenType())) {
            eof = getLexer().getTokenListener().onNewToken(eof);
            callback.accept(eof);
        }
    }

    @Override
    protected boolean step(PushbackReader pushbackReader, Consumer<Token> callback) throws IOException {
        int c = pushbackReader.read();

        if (c == -1) {
            if (candidate == null) {
                if (activeStates.size() != 1 || activeStates.iterator().next() != initial) {
                    throw new LexicalError(c, position);
                }
            } else {
                emit(pushbackReader, c, callback);
            }
            emitEof(callback);
            return false;
        }

        //update matched text
        matched.append((char) c);

        //apply new states
        Set<State<Character>> newStates = new HashSet<>();
        for (State<Character> state : activeStates) {
            for (Transition<Character> t : state.getOutgoingTransitions()) {
                if (t.isValid((char) c)) {
                    newStates.add(t.getNextState());
                }
            }
        }

        //no transition !
        if (newStates.isEmpty()) {

            if (candidate == null) {
                throw new LexicalError(c, position);
            } else {
                emit(pushbackReader, c, callback);
                return true;
            }
        }

        //find the state that is final with the highest priority
        List<State> posssibleFinals = newStates.stream()
                .filter(s -> s.isFinalState())
                .sorted(Comparator.comparingInt(s -> -getLexer().getPriority(s.getTerminal())))
                .limit(1)
                .collect(Collectors.toList());

        State<Character> finalState = posssibleFinals.isEmpty() ? null : posssibleFinals.get(0);

        if (finalState != null) {
            candidate = new Token(finalState.getTerminal(), startPosition, matched.toString());
        }

        this.position = this.position.updated((char) c);
        this.activeStates = newStates;
        return true;
    }

    class StateImpl implements State<Character> {

        private Set<Transition<Character>> outgoingTransitions = new HashSet<>();

        private Terminal terminal;

        private int id;

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
    }

    public State<Character> getInitialState() {
        return initial;
    }
}
