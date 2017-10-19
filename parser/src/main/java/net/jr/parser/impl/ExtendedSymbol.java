package net.jr.parser.impl;

import net.jr.common.Symbol;
import net.jr.util.StringUtil;

public class ExtendedSymbol implements Symbol {

    private int from;

    private Symbol symbol;

    private int to;

    public ExtendedSymbol(int from, Symbol symbol, int to) {
        this.from = from;
        this.symbol = symbol;
        this.to = to;
    }

    @Override
    public int hashCode() {
        return 613 + from + symbol.hashCode() + to;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!obj.getClass().equals(ExtendedSymbol.class)) {
            return false;
        }

        final ExtendedSymbol o = (ExtendedSymbol) obj;
        return o.from == from && o.symbol.equals(symbol) && o.to == to;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    @Override
    public boolean isTerminal() {
        return symbol.isTerminal();
    }

    @Override
    public String toString() {
        return StringUtil.toSubscript(from) + symbol.toString() + StringUtil.toSubscript(to);
    }
}
