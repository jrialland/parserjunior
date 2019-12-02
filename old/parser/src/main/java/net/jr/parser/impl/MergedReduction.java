package net.jr.parser.impl;

import net.jr.common.Symbol;
import net.jr.parser.Rule;

import java.util.Set;

/**
 * Step 2 of the reductions merge step, as described <a href="https://web.cs.dal.ca/~sjackson/lalr1.html">here</a>
 */
public class MergedReduction {

    private Rule rule;

    private int finalState;

    private Set<Symbol> followSet;

    public MergedReduction(Rule rule, int finalState, Set<Symbol> followSet) {
        this.rule = rule;
        this.finalState = finalState;
        this.followSet = followSet;
    }

    public Set<Symbol> getFollowSet() {
        return followSet;
    }

    public Rule getRule() {
        return rule;
    }

    public int getFinalState() {
        return finalState;
    }

    @Override
    public int hashCode() {
        return rule.hashCode() + finalState + 56483;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !obj.getClass().equals(MergedReduction.class)) {
            return false;
        }
        final MergedReduction o = (MergedReduction) obj;
        return o.rule.equals(rule) && o.finalState == finalState;
    }

    @Override
    public String toString() {
        return rule + " " + followSet;
    }
}
