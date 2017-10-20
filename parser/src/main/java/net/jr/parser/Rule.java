package net.jr.parser;

import net.jr.common.Symbol;

public interface Rule {

    int getId();

    Symbol[] getClause();

    Symbol getTarget();

    Derivation getDerivation();
}
