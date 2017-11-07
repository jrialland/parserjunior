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

    private static final Logger getLog() {
        return LOGGER;
    }

    private Symbol subject;

    private Set<LazySet> composition = new HashSet<>();

    private Set<Symbol> resolution;

    public LazySet(Symbol subject) {
        this.subject = subject;
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

    public void setResolution(Set<Symbol> resolution) {
        this.resolution = resolution;
    }

    public boolean isResolved() {
        return resolution != null;
    }

    public Set<Symbol> getResolution() {
        if(resolution == null) {
            throw new IllegalStateException("Could not determine terminals for " + this.toString()+ " = " + compositionToString());
        }
        return resolution;
    }


    private static class Bus<E>{

        private Set<Consumer<E>> listeners = new HashSet<>();

        public void emit(E evt) {
            for(Consumer<E> consumer : listeners) {
                consumer.accept(evt);
            }
        }

        public void addListener(Consumer<E> listener) {
            listeners.add(listener);
        }

    };

    public static void resolveAll(Map<Symbol, FollowSet> map) {
        _resolveAll(map);

    }
    /**
     * Tries to resolve all the passed-in definitions
     *
     * @param map definitions
     * @throws RuntimeException when it fails at finding a resolution for every sets
     */
    private static void _resolveAll(Map<Symbol, FollowSet> map) {


        final Bus bus = new Bus();

        Set<FollowSet> propositions = new HashSet<>();

        for(LazySet ls : map.values()) {

            if(ls.composition.size() == 1) {
                bus.addListener(lazySet -> {
                    if(lazySet == ls) {
                        LazySet compo = ls.getComposition().iterator().next();
                        compo.setResolution(ls.resolution);
                        bus.emit(compo);
                    }
                });
            }

            if(!ls.isResolved()) {
                Set<LazySet> toResolve = ls.composition.stream().filter(s->!s.isResolved()).collect(Collectors.toSet());
                bus.addListener(lazySet -> {
                        toResolve.remove(lazySet);
                        if(toResolve.isEmpty() && !ls.isResolved()) {
                            ls.setResolved();
                            bus.emit(ls);
                        }
                    });
            }
        }

        for(LazySet ls : map.values()) {
            if(ls.isResolved()) {
                bus.emit(ls);
            }
        }
    }

    private void setResolved() {
        resolution = new HashSet<>();
        for(LazySet ls : composition) {
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
}
