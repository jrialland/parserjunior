package net.jr.lexer.automaton;

import net.jr.lexer.Terminal;
import net.jr.lexer.impl.CharConstraint;

import java.util.*;
import java.util.function.Function;

public class DefaultAutomaton implements Automaton {

    private static final StateImpl FailedState = new StateImpl(Collections.emptySet(), false, null);

    private Terminal tokenType;

    private State initialState;

    private DefaultAutomaton(Terminal tokenType, State initialState) {
        this.tokenType = tokenType;
        this.initialState = initialState;
    }

    @Override
    public State getInitialState() {
        return initialState;
    }

    @Override
    public Object clone() {
        return new DefaultAutomaton(tokenType, initialState);
    }

    public Terminal getTokenType() {
        return tokenType;
    }

    private static class StateImpl implements State, Cloneable {

        private Set<Transition> outgoingTransitions;

        private boolean finalState;

        private Terminal terminal;

        private StateImpl(Set<Transition> transitions, boolean finalState, Terminal terminal) {
            this.outgoingTransitions = transitions;
            this.finalState = finalState;
            this.terminal = terminal;
        }

        public static StateImpl finalState(Terminal terminal) {
            StateImpl stateImpl = new StateImpl(null, true, terminal);
            return stateImpl;
        }

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
            clones.put(this, new StateImpl(clonedTransitions, finalState, terminal));

            for (Transition t : clonedTransitions) {
                TransitionImpl tImpl = (TransitionImpl) t;
                StateImpl tNext = tImpl.nextState;
                Transition tClone = new TransitionImpl(tImpl.condition, tNext.clone(clones));
                clonedTransitions.add(tClone);
            }

            return clones.get(this);

        }

        public Set<Transition> getOutgoingTransitions() {
            return outgoingTransitions;
        }

        @Override
        public boolean isFinalState() {
            return finalState;
        }

        @Override
        public Terminal getTerminal() {
            return terminal;
        }
    }

    private static class TransitionImpl implements Transition<Character> {

        private Function<Character, Boolean> condition = x -> false;

        private StateImpl nextState;

        TransitionImpl(Function<Character, Boolean> condition, StateImpl nextState) {
            this.condition = condition;
            this.nextState = nextState;
        }

        public boolean isValid(Character c) {
            return condition.apply(c);
        }

        public StateImpl getNextState() {
            return nextState;
        }

    }

    public static class Builder {

        private Terminal tokenType;

        private BuilderStateImpl initialState = new BuilderStateImpl(new StateImpl(new HashSet<>(), false, null));

        private Builder(Terminal tokenType) {
            this.tokenType = tokenType;
        }

        public static Builder forTokenType(Terminal tokenType) {
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
