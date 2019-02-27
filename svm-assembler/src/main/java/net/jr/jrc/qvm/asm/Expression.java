package net.jr.jrc.qvm.asm;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Expression {

    private String expr;

    private Set<String> refs = new HashSet<>();

    public Expression(String expr) {
        this.expr = expr;
    }

    public void setExpr(String expr) {
        this.expr = expr;
    }

    public String getExpr() {
        return expr;
    }

    public Set<String> getRefs() {
        return refs;
    }

    public void setRefs(Set<String> refs) {
        this.refs = refs;
    }


    public int compute(Map<String, Integer> knownValues) {

        if(expr.matches("^-?[0-9]+$")) {
            return Integer.parseInt(expr);
        }

        else if(expr.startsWith("@") && knownValues.containsKey(expr.substring(1))){
            return knownValues.get(expr.substring(1));
        }
        throw new IllegalStateException();
    }
}
