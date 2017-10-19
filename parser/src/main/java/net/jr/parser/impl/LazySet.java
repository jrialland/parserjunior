package net.jr.parser.impl;

import net.jr.common.Symbol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

public abstract class LazySet {

    private static final Logger Logger = LoggerFactory.getLogger(LazySet.class);

    private static final Logger getLog() {
        return Logger;
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
        if (resolution != null) {
            throw new IllegalStateException("Definition cannot be changed once solved");
        }
        composition.add(lazySet);
    }

    public void setResolution(Set<Symbol> resolution) {
        this.resolution = resolution;
    }

    public Set<Symbol> getResolution() {
        return resolution;
    }

    private boolean isSynonymOf(Collection<? extends LazySet> allEqs, LazySet l) {
        if(composition.size() == 1 && composition.iterator().next().equals(l)) {
            return true;
        }
        if(l.composition.size() == 1 && l.composition.iterator().next().equals(this)) {
            return true;
        }
        return false;
    }

    private boolean simplify(Collection<? extends LazySet> allEqs) {

        //if already resolved, just return true
        if(resolution != null) {
            return true;
        } else {

            if(composition.size() > 1) {
                Iterator<LazySet> it = composition.iterator();
                while (it.hasNext()) {
                    LazySet n = it.next();
                    if (n.isSynonymOf(allEqs, this)) {
                        it.remove();
                    }
                }
            }

            Set<Symbol> attempt = new HashSet<>();
            for(LazySet l : composition) {
                if(l.resolution == null) {
                    return false;
                }
                attempt.addAll(l.resolution);
            }
            this.resolution = attempt;
            return true;
        }
    }


    public static void resolveAll(Collection<? extends LazySet> lazySets) {
        int size = lazySets.size();
        int solvedInPrecRound, solvedInThisRound = 0;
        do {
            solvedInPrecRound = solvedInThisRound;
            solvedInThisRound = 0;
            for (LazySet l : lazySets) {
                solvedInThisRound += l.simplify(lazySets)?1:0;
            }
            getLog().debug(String.format("Resolving : %d/%d", solvedInThisRound, size));
        } while(solvedInThisRound < size || solvedInPrecRound == solvedInThisRound);

        if(solvedInThisRound < size) {
            String message = String.format("Resolution failure ! (only %d/%d solved)", solvedInThisRound, size);
            StringWriter sw = new StringWriter();
            sw.append(message + "\n");
            for(LazySet l : lazySets) {
                sw.append("    " + l.toString() + " = " + l.compositionToString());
                sw.append("\n");
            }
            getLog().error(sw.toString());
            throw new RuntimeException(message);
        }

    }

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
