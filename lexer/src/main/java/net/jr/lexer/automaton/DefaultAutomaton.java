package net.jr.lexer.automaton;

import net.jr.lexer.Lexeme;
import net.jr.lexer.impl.CharConstraint;

import java.util.*;
import java.util.function.Function;

public class DefaultAutomaton implements Automaton {

    private static final StateImpl FailedState = new StateImpl(Collections.emptySet(), false, null);

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
    public State getInitialState() {
        return initialState;
    }

    @Override
    public Object clone() {
        return new DefaultAutomaton(tokenType, initialState);
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
        Set<Transition> transitions = currentState.getOutgoingTransitions();
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
        return currentState.isFinalState();
    }

    private static class StateImpl implements State, Cloneable {

        private Set<Transition> outgoingTransitions;

        private boolean finalState;

        private Lexeme lexeme = null;

        private StateImpl clone(Map<StateImpl, StateImpl> knownClones) throws CloneNotSupportedException {
            StateImpl s = knownClones.get(this);
            if (s != null) {
                return s;
            } else {
                return (StateImpl) clone();
            }
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {

            Set<Transition> clonedTransitions = new HashSet<>();
            Map<StateImpl, StateImpl> clones = new HashMap<>();
            clones.put(this, new StateImpl(clonedTransitions, finalState, lexeme));

            for (Transition t : clonedTransitions) {
                TransitionImpl tImpl = (TransitionImpl)t;
                StateImpl tNext = tImpl.nextState;
                Transition tClone = new TransitionImpl(tImpl.condition, tNext.clone(clones));
                clonedTransitions.add(tClone);
            }

            return clones.get(this);

        }

        private StateImpl(Set<Transition> transitions, boolean finalState, Lexeme lexeme) {
            this.outgoingTransitions = transitions;
            this.finalState = finalState;
            this.lexeme = lexeme;
        }

        public static StateImpl nonFinal(Set<Transition> outgoingTransitions) {
            StateImpl stateImpl = new StateImpl(outgoingTransitions, false, null);
            stateImpl.outgoingTransitions = outgoingTransitions;
            stateImpl.finalState = false;
            return stateImpl;
        }

        public static StateImpl finalState(Lexeme lexeme) {
            StateImpl stateImpl = new StateImpl(null, true, lexeme);
            return stateImpl;
        }

        public Set<Transition> getOutgoingTransitions() {
            return outgoingTransitions;
        }

        @Override
        public boolean isFinalState() {
            return finalState;
        }

        @Override
        public Lexeme getLexeme() {
            return lexeme;
        }
    }

    private static class TransitionImpl implements Transition<Character> {

        private Function<Character, Boolean> condition = x -> false;

        private StateImpl nextState;

        TransitionImpl(Function<Character, Boolean> condition, StateImpl nextState) {
            this.condition = condition;
            this.nextState = nextState;
        }

        public boolean isValid(char c) {
            return condition.apply(c);
        }

        public StateImpl getNextState() {
            return nextState;
        }

        public Function<Character, Boolean> getCondition() {
            return condition;
        }
    }

    public static class Builder {

        private Lexeme tokenType;

        private BuilderStateImpl initialState = new BuilderStateImpl(new StateImpl(new HashSet<>(), false, null));

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
            return new BuilderStateImpl(new StateImpl(new HashSet<>(), false, null));
        }

        public BuilderState newFinalState() {
            assert tokenType != null;
            return new BuilderStateImpl(new StateImpl(new HashSet<>(), true, tokenType));
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

            private StateImpl state;

            BuilderStateImpl(StateImpl state) {
                this.state = state;
            }

            public BuilderTransition when(CharConstraint.Builder conditionBuilder) {
                return new BuilderTransitionImpl(state, conditionBuilder.build());
            }
        }

        class BuilderTransitionImpl implements BuilderTransition {

            private StateImpl state;

            private CharConstraint condition;

            BuilderTransitionImpl(StateImpl state, CharConstraint condition) {
                this.state = state;
                this.condition = condition;
            }

            @Override
            public void goTo(BuilderState destination) {
                TransitionImpl transition = new TransitionImpl(condition, ((BuilderStateImpl) destination).state);
                state.outgoingTransitions.add(transition);
            }
        }

    }


}
