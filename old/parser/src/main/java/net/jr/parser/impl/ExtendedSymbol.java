package net.jr.parser.impl;

import net.jr.common.Symbol;
import net.jr.common.SymbolBase;
import net.jr.marshalling.MarshallingUtil;
import net.jr.parser.Rule;
import net.jr.util.StringUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Set;

/**
 * An {@link ExtendedSymbol} is the type of symbol that are used in 'Extended' grammars. It keeps track of the way
 * a particular production is obtained according to a 'base' grammar.
 *
 * @see ActionTable.LALR1Builder#makeExtendedGrammar(Rule, Set)
 */
public class ExtendedSymbol extends SymbolBase {

    private int from;

    private Symbol symbol;

    private int to;

    public ExtendedSymbol(int from, Symbol symbol, int to) {
        this.from = from;
        this.symbol = symbol;
        this.to = to;
    }

    @SuppressWarnings("unused")
    public static ExtendedSymbol unMarshall(DataInput in) throws IOException {
        int from = in.readInt();
        Symbol symbol = MarshallingUtil.unMarshall(in);
        int to = in.readInt();
        return new ExtendedSymbol(from, symbol, to);
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
        return StringUtil.toSubscript(getFrom()) + getSymbol().toString() + StringUtil.toSubscript(getTo());
    }

    @Override
    public void marshall(DataOutput dataOutputStream) throws IOException {
        dataOutputStream.writeInt(from);
        symbol.marshall(dataOutputStream);
        dataOutputStream.writeInt(to);
    }
}
