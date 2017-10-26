package net.jr.parser.impl;

import net.jr.common.Symbol;
import net.jr.parser.Grammar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Helper class for lazy computation of inclusions involving the computation of FIRST and FOLLOW sets.
 * It allow to define a 'Set' of {@link Symbol}s to be defined as being the union of other sets.
 * <p>
 * Some set are also given a 'real' value by calling the {@link LazySet#setResolution(Set)} method.
 * <p>
 * When all the sets ared defined, one may call {@link LazySet#resolveAll(Collection)} in order to try to give a 'real' value (aka a 'resolution')
 * to every sets.
 *
 * @see ActionTable.LALR1Builder#getFollowSets(Grammar, Symbol)
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

    public void add(LazySet lazySet) {
        composition.add(lazySet);
    }

    public void setResolution(Set<Symbol> resolution) {
        this.resolution = resolution;
    }

    public Set<Symbol> getResolution() {
        return resolution;
    }

    /**
     * Tries to replace the definition of the set by its real value
     *
     * @param allEqs the other definitions
     * @return true is the set is resolved
     */
    private boolean simplify(Collection<? extends LazySet> allEqs) {

        //dont not include self
        composition.remove(this);

        //if already resolved, just return true
        if (resolution != null) {

            if(resolution.isEmpty()) {
                for(LazySet l : allEqs) {
                    l.composition.remove(this);
                }
            }
            return true;

        } else {

            if (composition.size() > 1) {
                Iterator<LazySet> it = composition.iterator();
                while (it.hasNext()) {
                    LazySet n = it.next();
                    if(n.resolution != null && n.resolution.isEmpty()) {
                        it.remove();
                        continue;
                    }
                    if(n.composition.contains(this)) {
                        n.composition.remove(this);
                        continue;
                    }
                }
            }

            Set<Symbol> attempt = new HashSet<>();
            for (LazySet l : composition) {
                if (l.resolution == null) {
                    return false;
                }
                attempt.addAll(l.resolution);
            }
            this.resolution = attempt;
            return true;
        }
    }

    /**
     * Tries to resolve all the passed-in definitions
     *
     * @param lazySets definitions
     * @throws RuntimeException when it fails at finding a resolution for every sets
     */
    public static void resolveAll(Collection<? extends LazySet> lazySets) {
        int size = lazySets.size();
        int solvedInPrecRound, solvedInThisRound = 0;
        do {
            solvedInPrecRound = solvedInThisRound;
            solvedInThisRound = 0;
            for (LazySet l : lazySets) {
                solvedInThisRound += l.simplify(lazySets) ? 1 : 0;
            }
        } while (solvedInThisRound < size && solvedInPrecRound < solvedInThisRound);

        if (solvedInThisRound < size) {
            String message = String.format("Resolution failure ! (only %d/%d solved)", solvedInThisRound, size);
            StringWriter sw = new StringWriter();
            sw.append(message + "\n");
            for (LazySet l : lazySets) {
                sw.append("    " + l.toString() + " = " + l.compositionToString());
                sw.append("\n");
            }
            getLog().error(sw.toString());
            throw new RuntimeException(message);
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
                return String.join(" ∪ ", composition.stream().map(s -> s.toString()).collect(Collectors.toList()));
            }
        }
    }
}
