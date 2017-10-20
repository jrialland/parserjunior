package net.jr.parser.impl;

import net.jr.common.Symbol;
import net.jr.parser.Derivation;
import net.jr.parser.Rule;

public class ExtendedRule implements Rule {

    private int id;

    private Rule parentRule;

    private ExtendedSymbol target;

    private ExtendedSymbol[] clause;

    public ExtendedRule(int id, Rule parentRule, ExtendedSymbol target, ExtendedSymbol[] clause) {
        this.id = id;
        this.parentRule = parentRule;
        this.target = target;
        this.clause = clause;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Symbol getTarget() {
        return target;
    }

    @Override
    public Symbol[] getClause() {
        return clause;
    }

    public boolean isExtensionOf(Rule rule) {
        return this.parentRule.equals(rule);
    }

    public int getFinalState() {
        return clause[clause.length-1].getTo();
    }

    @Override
    public Derivation getDerivation() {
        throw new UnsupportedOperationException();
    }
}
