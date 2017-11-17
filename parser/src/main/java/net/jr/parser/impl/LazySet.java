package net.jr.parser.impl;

import net.jr.common.Symbol;
import net.jr.parser.Grammar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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

    public LazySet(Symbol subject) {
        this.subject = subject;
    }

    private static final Logger getLog() {
        return LOGGER;
    }

    public static void resolveAll(Map<Symbol, ? extends LazySet> map) {
        getLog().trace(String.format("%d/%d", map.values().stream().filter(s -> s.isResolved()).count(), map.size()));
        int total = map.size(), lastResolved = 0, resolved = simplify(map.values());
        while (resolved < total && lastResolved != resolved) {
            getLog().trace(String.format("%d/%d", resolved, total));
            lastResolved = resolved;
            for (LazySet f : map.values()) {
                f.getComposition().remove(f);//do not include self
                for (LazySet f2 : map.values()) {
                    if (f != f2) {
                        if (f2.getComposition().remove(f)) {
                            f2.getComposition().addAll(f.getComposition());
                        }
                    }
                }
            }
            resolved = simplify(map.values());
        }

        getLog().trace(String.format("%d/%d", resolved, total));

        if (resolved < total) {

            if (getLog().isDebugEnabled()) {
                StringWriter sw = new StringWriter();
                sw.append("Unresolved expressions :\n");
                for (LazySet f : map.values()) {
                    if (!f.isResolved()) {
                        sw.append(f.toString() + " = " + f.compositionToString());
                        sw.append("\n");
                    }
                }
                getLog().debug(sw.toString());
            }

            throw new IllegalStateException("Could not compute 'Follow' sets, please check your grammar");
        }

    }

    /**
     * Simplifies the definitions of the follow sets where possible
     *
     * @param sets
     * @return
     */
    private static <L extends LazySet> int simplify(Collection<L> sets) {
        final EvtSupport<LazySet> bus = new EvtSupport<>();

        for (L ls : sets) {
            if (!ls.isResolved()) {
                //the list that has to be resolved in order to consider ls resolved
                Set<LazySet> toResolve = ls.getComposition().stream().filter(s -> !s.isResolved()).collect(Collectors.toSet());

                bus.addListener(lazySet -> {
                    //remove from the list
                    toResolve.remove(lazySet);
                    //if the list is empty, we consider that ls is now resolved
                    if (toResolve.isEmpty() && !ls.isResolved()) {
                        ls.setResolved();
                        bus.emit(ls);
                    }
                });
            }
        }

        int count = 0;
        for (LazySet ls : sets) {
            if (ls.isResolved()) {
                bus.emit(ls);
                count++;
            }
        }
        return count;

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

    protected void setResolved() {
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
