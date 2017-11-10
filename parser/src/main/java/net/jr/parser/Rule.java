package net.jr.parser;

import net.jr.common.Symbol;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * a Rule is the base construction of a grammar. it is the association of
 * a Goal and its 'components' aka 'clause' aka 'predecessors', i.e. the list of symbol that may be used to form the goal.
 */
public abstract class Rule {

    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public abstract Symbol[] getClause();

    public abstract Symbol getTarget();

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
