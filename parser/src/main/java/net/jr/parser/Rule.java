package net.jr.parser;

import net.jr.common.Symbol;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class Rule {

    public abstract int getId();

    public abstract Symbol[] getClause();

    public abstract Symbol getTarget();

    public abstract Derivation getDerivation();

    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        sw.append("(");
        sw.append(Integer.toString(getId()));
        sw.append(") ");
        sw.append(getTarget().toString());
        sw.append(" → ");
        Symbol[] clause = getClause();
        if (clause.length == 0) {
            sw.append("ε");
        } else {
            sw.append(String.join(" ", Arrays.asList(clause).stream().map(s -> s.toString()).collect(Collectors.toList())));
        }
        sw.append(".");
        return sw.toString();
    }
}
