package net.jr.parser.impl;

import net.jr.common.Symbol;
import net.jr.parser.Grammar;

import java.io.StringWriter;

public class Item {

    private Grammar.Rule rule;

    private int pointer;

    public Item(Grammar.Rule rule, int pointer) {
        assert rule != null;
        assert pointer >= 0;
        assert pointer <= rule.getClause().length;
        this.rule = rule;
        this.pointer = pointer;
    }

    @Override
    public int hashCode() {
        return rule.hashCode() - 7 * pointer;
    }

    public Grammar.Rule getRule() {
        return rule;
    }

    public int getPointer() {
        return pointer;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o != null && o.getClass().equals(Item.class))) {
            return false;
        }
        final Item oItem = (Item) o;
        return oItem.rule.equals(rule) && oItem.pointer == pointer;
    }

    public Symbol getExpectedSymbol() {
        if (pointer == rule.getClause().length) {
            return null;
        }
        return rule.getClause()[pointer];
    }

    /**
     *
     * @return true when the pointer is at the end of the item's clause
     */
    public boolean isFinished() {
        return pointer == rule.getClause().length;
    }

    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        sw.append(rule.getTarget().toString());
        sw.append(" →");
        Symbol[] syms = rule.getClause();
        for (int i = 0; i < syms.length; i++) {
            if (i == pointer) {
                sw.append(" •");
            }
            sw.append(" ");
            sw.append(syms[i].toString());
        }
        if (pointer == syms.length) {
            sw.append(" •");
        }
        return sw.toString();
    }
}
