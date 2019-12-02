package net.jr.parser.impl;

import net.jr.common.Symbol;
import net.jr.parser.Rule;

import java.util.Set;

/**
 * (Step 4 - Reductions, Sub-Step 1), as described <a href="https://web.cs.dal.ca/~sjackson/lalr1.html">here</a>
 */
public class PreMergeReduction {

    private ExtendedRule extendedRule;

    private Set<Symbol> followSet;

    public PreMergeReduction(ExtendedRule extendedRule, Set<Symbol> followSet) {
        this.extendedRule = extendedRule;
        this.followSet = followSet;
    }

    public boolean matches(PreMergeReduction o) {
        boolean sameFinalState = extendedRule.getFinalState() == o.extendedRule.getFinalState();
        return sameFinalState && extendedRule.isExtensionOf(o.extendedRule.getBaseRule());
    }

    public Set<Symbol> getFollowSet() {
        return followSet;
    }

    public Rule getBaseRule() {
        return extendedRule.getBaseRule();
    }

    public int getFinalState() {
        return extendedRule.getFinalState();
    }

    @Override
    public String toString() {
        return extendedRule.toString() + "  " + followSet;
    }
}
