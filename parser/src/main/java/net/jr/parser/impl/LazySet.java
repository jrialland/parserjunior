package net.jr.parser.impl;

import net.jr.common.Symbol;
import net.jr.parser.Grammar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Helper class for lazy computation of inclusions involving the computation of FIRST and FOLLOW sets.
 * It allow to define a 'Set' of {@link Symbol}s to be defined as being the union of other sets.
 * <p>
 * Some set are also given a 'real' value by calling the {@link LazySet#setResolution(Set)} method.
 * <p>
 * When all the sets ared defined, one may call {@link LazySet#resolveAll(Map)} in order to try to give a 'real' value (aka a 'resolution')
 * to every sets.
 *
 * @see ActionTable.LALR1Builder#getFollowSets(Grammar)
 */
public abstract class LazySet {

    private static final Logger LOGGER = LoggerFactory.getLogger(LazySet.class);

    private Symbol subject;

    private Set<LazySet> composition = new HashSet<>();

    private Set<Symbol> resolution;

    private Stack<LazySet> redefs = new Stack<>();

    private Stack<Boolean> bRedefs = new Stack<>();

    public LazySet(Symbol subject) {
        this.subject = subject;
    }

    private static final Logger getLog() {
        return LOGGER;
    }

    public static void resolveAll(Map<Symbol, FollowSet> map) {
        System.out.println(String.format("%d/%d", map.values().stream().filter(s->s.isResolved()).count(), map.size()));
        int total = map.size(), lastResolved=0, resolved = simplify(map.values());
        while(resolved < total && lastResolved != resolved) {
            lastResolved = resolved;
        }
        System.out.println(String.format("%d/%d", resolved, total));
    }

    /**
     * Tries to resolve all the passed-in definitions
     *
     * @param map definitions
     * @throws RuntimeException when it fails at finding a resolution for every sets
     */
    private static int simplify(Collection<FollowSet> sets) {
        final EvtSupport bus = new EvtSupport();
        for (LazySet ls : sets) {
            if (!ls.isResolved()) {
                Set<LazySet> toResolve = ls.composition.stream().filter(s -> !s.isResolved()).collect(Collectors.toSet());
                bus.addListener(lazySet -> {
                    toResolve.remove(lazySet);
                    if (toResolve.isEmpty() && !ls.isResolved()) {
                        ls.setResolved();
                        bus.emit(ls);
                    }
                });
            }
        }
        int count = 0;
        for (LazySet ls : sets) {
            ls.getComposition().remove(ls);
            if (ls.isResolved()) {
                bus.emit(ls);
                count++;
            }
        }
        return count;
    }

    protected void pushAltDefinition(LazySet def) {
        redefs.add(def);
        if (resolution == null && composition.remove(def)) {
            bRedefs.add(true);
            composition.addAll(def.getComposition());
        } else {
            bRedefs.add(false);
        }
    }

    protected void popAltDefinition() {
        LazySet def = redefs.pop();
        if (bRedefs.pop()) {
            composition.removeAll(def.getComposition());
            composition.add(def);
        }
    }

    public Symbol getSubject() {
        return subject;
    }

    public Set<LazySet> getComposition() {
        return composition;
    }

    public void add(LazySet lazySet) {
        composition.add(lazySet);
    }

    public boolean isResolved() {
        return resolution != null;
    }

    public Set<Symbol> getResolution() {
        if (resolution == null) {
            throw new IllegalStateException("Could not determine terminals for " + this.toString() + " = " + compositionToString());
        }
        return resolution;
    }

    ;

    public void setResolution(Set<Symbol> resolution) {
        this.resolution = resolution;
    }

    private void setResolved() {
        resolution = new HashSet<>();
        for (LazySet ls : composition) {
            resolution.addAll(ls.getResolution());
        }
    }

    /**
     * Dumps the definition of this {@link LazySet}
     *
     * @return
     */
    public String compositionToString() {

        if (resolution != null) {
            return resolution.toString();
        } else {
            if (composition.isEmpty()) {
                return "[]";
            } else {
                return String.join(" ∪ ", composition.stream().map(s -> s.toString() + (s.isResolved() ? "*" : "")).collect(Collectors.toList()));
            }
        }
    }

    private static class EvtSupport<E> {

        private Set<Consumer<E>> listeners = new HashSet<>();

        public void emit(E evt) {
            for (Consumer<E> consumer : listeners) {
                consumer.accept(evt);
            }
        }

        public void addListener(Consumer<E> listener) {
            listeners.add(listener);
        }

    }
}
