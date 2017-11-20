package net.jr.parser.impl;

import net.jr.common.Symbol;
import net.jr.parser.Rule;

import java.util.Set;

/**
 * Implementation of the @link {@link Rule} interface that wrap instances of @link {@link ExtendedSymbol}.
 * This this of rule is used while contructing the extended grammar exploited for computing Reduce decisions
 * during the construction of "LALR(1)" (-by SLR) parse table
 *
 * @see ActionTable.LALR1Builder#makeExtendedGrammar(Rule, Set) for usage details
 */
public class ExtendedRule extends Rule {

    private Rule baseRule;

    private ExtendedSymbol target;

    private ExtendedSymbol[] clause;

    public ExtendedRule(int id, Rule baseRule, ExtendedSymbol target, ExtendedSymbol[] clause) {
        setId(id);
        this.baseRule = baseRule;
        this.target = target;
        this.clause = clause;
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
        return this.baseRule.equals(rule);
    }

    public int getFinalState() {
        return clause[clause.length - 1].getTo();
    }

    public Rule getBaseRule() {
        return baseRule;
    }

    @Override
    public String getName() {
        return baseRule.getName();
    }
}
