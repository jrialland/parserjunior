package net.jr.parser.impl;

import net.jr.common.Symbol;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ItemSet {

    private int id;

    private Set<Item> kernel;

    private Set<Item> members;

    private Map<Symbol, ItemSet> transitions;

    public ItemSet(int id, Set<Item> kernel, Set<Item> members) {
        this.id = id;
        this.kernel = kernel;
        this.members = members;
        this.transitions = new HashMap<>();
    }

    public void addTransition(Symbol symbol, ItemSet targetItemsSet) {
        ItemSet oldItemSet = transitions.put(symbol, targetItemsSet);
        if(oldItemSet != null) {
            assert oldItemSet.equals(targetItemsSet);
        }
    }

    public ItemSet getItemSetForTransition(Symbol s) {
        return transitions.get(s);
    }

    /**
     *
     * The kernel is the only thing that should be used for equals and hashcode
     * @return
     */
    @Override
    public int hashCode() {
        return kernel.hashCode() ^ 23;
    }

    /**
     * The kernel is the only thing that should be used for equals and hashcode
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (!(o != null && o.getClass().equals(ItemSet.class))) {
            return false;
        }
        final ItemSet oItemSet = (ItemSet) o;
        return oItemSet.kernel.equals(kernel);
    }

    public Iterable<Symbol> getPossibleNextSymbols() {
        Set<Symbol> set = new HashSet<>();
        Symbol s;
        for (Item i : kernel) {
            s = i.getExpectedSymbol();
            if (s != null) {
                set.add(s);
            }
        }
        for (Item i : members) {
            s = i.getExpectedSymbol();
            if (s != null) {
                set.add(s);
            }
        }
        return set;
    }

    public Set<Item> getItemsThatExpect(Symbol symbol) {
        Set<Item> set = new HashSet<>();
        Symbol s;
        for (Item item : kernel) {
            s = item.getExpectedSymbol();
            if (s != null && symbol.equals(s)) {
                set.add(item);
            }
        }
        for (Item item : members) {
            s = item.getExpectedSymbol();
            if (s != null && symbol.equals(s)) {
                set.add(item);
            }
        }
        return set;
    }

    public String getName() {
        return "I"+id;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        sw.append(getName() + " : {\n");
        for (Item item : kernel) {
            sw.append("   " + item.toString() + "\n");
        }
        for (Item item : members) {
            sw.append("    + " + item.toString() + "\n");
        }
        sw.append("}");

        sw.append(", transitions = ");
        sw.append(transitions.entrySet().stream().map(e->e.getKey().toString()+"->"+e.getValue().getName()).collect(Collectors.toList()).toString());

        return sw.toString();
    }
}
