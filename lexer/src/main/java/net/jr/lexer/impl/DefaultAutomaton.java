package net.jr.lexer.impl;

import net.jr.lexer.Lexeme;

import java.util.*;
import java.util.function.Function;

public class DefaultAutomaton implements Automaton {

    private static final State FailedState = new State(Collections.emptyList(), false);

    private Lexeme tokenType;

    private State initialState;

    private State currentState;

    private String matchedString = "";

    private DefaultAutomaton(Lexeme tokenType, State initialState) {
        this.tokenType = tokenType;
        this.initialState = initialState;
        this.currentState = initialState;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return new DefaultAutomaton(tokenType, initialState);
    }

    public void setTokenType(Lexeme tokenType) {
        this.tokenType = tokenType;
    }

    public Lexeme getTokenType() {
        return tokenType;
    }

    /**
     * @param c
     * @return true if the automaton is "dead"
     */
    @Override
    public boolean step(char c) {
        if (currentState == FailedState) {
            return true;
        }
        List<Transition> transitions = currentState.getOutgoingTransitions();
        currentState = FailedState;
        for (Transition transition : transitions) {
            if (transition.isValid(c)) {
                matchedString += c;
                currentState = transition.getNextState();
                break;
            }
        }
        return currentState == FailedState;
    }

    @Override
    public void reset() {
        currentState = initialState;
        this.matchedString = "";
    }

    @Override
    public int getMatchedLength() {
        return matchedString.length();
    }

    public boolean isInFinalState() {
        return currentState.finalState;
    }

    private static class State implements Cloneable {

        private List<Transition> outgoingTransitions;

        private boolean finalState;

        private State clone(Map<State, State> knownClones) throws CloneNotSupportedException {
            State s = knownClones.get(this);
            if (s != null) {
                return s;
            } else {
                return (State) clone();
            }
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {

            List<Transition> clonedTransitions = new ArrayList<>();
            Map<State, State> clones = new HashMap<>();
            clones.put(this, new State(clonedTransitions, finalState));

            for (Transition t : clonedTransitions) {
                Transition tClone = new Transition(t.condition, t.nextState.clone(clones));
                clonedTransitions.add(tClone);
            }

            return clones.get(this);

        }

        public State(List<Transition> outgoingTransitions, boolean finalState) {
            this.outgoingTransitions = outgoingTransitions;
            this.finalState = finalState;
        }

        public List<Transition> getOutgoingTransitions() {
            return outgoingTransitions;
        }
    }

    private static class Transition {

        private Function<Character, Boolean> condition = x -> false;

        private State nextState;

        Transition(Function<Character, Boolean> condition, State nextState) {
            this.condition = condition;
            this.nextState = nextState;
        }

        public boolean isValid(char c) {
            return condition.apply(c);
        }

        public State getNextState() {
            return nextState;
        }
    }

    public static class Builder {

        private Lexeme tokenType;

        private BuilderStateImpl initialState = new BuilderStateImpl(new State(new ArrayList<>(), false));

        private Builder(Lexeme tokenType) {
            this.tokenType = tokenType;
        }

        public static Builder forTokenType(Lexeme tokenType) {
            return new Builder(tokenType);
        }

        public BuilderState initialState() {
            return initialState;
        }

        public BuilderState newNonFinalState() {
            return new BuilderStateImpl(new State(new ArrayList<>(), false));
        }

        public BuilderState newFinalState() {
            return new BuilderStateImpl(new State(new ArrayList<>(), true));
        }

        public BuilderState failedState() {
            return new BuilderStateImpl(FailedState);
        }

        public Automaton build() {
            return new DefaultAutomaton(tokenType, initialState.state);
        }

        public interface BuilderState {

            BuilderTransition when(CharConstraint.Builder builder);
        }

        public interface BuilderTransition {
            void goTo(BuilderState destination);
        }

        class BuilderStateImpl implements BuilderState {

            private State state;

            BuilderStateImpl(State state) {
                this.state = state;
            }

            public BuilderTransition when(CharConstraint.Builder conditionBuilder) {
                return new BuilderTransitionImpl(state, conditionBuilder.build());
            }
        }

        class BuilderTransitionImpl implements BuilderTransition {

            private State state;

            private CharConstraint condition;

            BuilderTransitionImpl(State state, CharConstraint condition) {
                this.state = state;
                this.condition = condition;
            }

            @Override
            public void goTo(BuilderState destination) {
                Transition transition = new Transition(condition, ((BuilderStateImpl) destination).state);
                state.outgoingTransitions.add(transition);
            }
        }

    }


}
