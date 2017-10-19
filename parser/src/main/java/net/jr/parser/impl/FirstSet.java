package net.jr.parser.impl;

import net.jr.common.Symbol;

public class FirstSet extends LazySet {

    public FirstSet(Symbol s) {
        super(s);
    }

    @Override
    public int hashCode() {
        return super.hashCode() + 17;
    }

    @Override
    public String toString() {
        return String.format("FirstSet(%s)", getSubject());
    }
}
