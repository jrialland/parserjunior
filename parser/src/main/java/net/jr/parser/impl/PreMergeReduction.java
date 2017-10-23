package net.jr.parser.impl;

import net.jr.common.Symbol;
import net.jr.parser.Rule;

import java.util.Set;

public class PreMergeReduction {

    private ExtendedRule extendedRule;

    private Set<Symbol> followSet;

    public PreMergeReduction(ExtendedRule extendedRule, Set<Symbol> followSet) {
        this.extendedRule = extendedRule;
        this.followSet = followSet;
    }

    public boolean matches(PreMergeReduction o) {
        boolean sameFinalState =  extendedRule.getFinalState() == o.extendedRule.getFinalState();
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
