package net.jr.parser.impl;

import net.jr.common.Symbol;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Collection of {@link Item}. the 'kernel' and the 'members' form a closure.
 * <p>
 * (Implementation note) : The identity of an ItemSet is based on the kernel, and not the id or members
 */
public class ItemSet {

    private int id = -1;

    private Set<Item> kernel;

    private Set<Item> members;

    private Map<Symbol, ItemSet> transitions;

    public ItemSet(Set<Item> kernel, Set<Item> members) {
        this.kernel = kernel;
        this.members = members;
        this.transitions = new HashMap<>();
    }

    public Set<Item> getKernel() {
        return kernel;
    }

    public void addTransition(Symbol symbol, ItemSet targetItemsSet) {
        ItemSet oldItemSet = transitions.put(symbol, targetItemsSet);
        if (oldItemSet != null) {
            assert oldItemSet.equals(targetItemsSet);
        }
    }

    public ItemSet getTransitionFor(Symbol symbol) {
        return transitions.get(symbol);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * The kernel is the only thing that should be used for equals and hashcode
     *
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
        return allItems()
                .map(Item::getExpectedSymbol)
                .filter(s -> s != null).collect(Collectors.toSet());
    }

    public Set<Item> getItemsThatExpect(final Symbol symbol) {
        return allItems().filter(i -> {
            Symbol s = i.getExpectedSymbol();
            return s != null && s.equals(symbol);
        }).collect(Collectors.toSet());
    }

    public Stream<Item> allItems() {
        return Stream.concat(kernel.stream(), members.stream());
    }

    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        if (getId() != -1) {
            sw.append("I" + getId() + " : ");
        }
        sw.append("{\n");
        for (Item item : kernel) {
            sw.append("   " + item.toString() + "\n");
        }
        for (Item item : members) {
            sw.append("    + " + item.toString() + "\n");
        }
        sw.append("}");

        sw.append(", transitions = ");
        sw.append(transitions.entrySet().stream().map(e -> e.getKey().toString() + "->I" + e.getValue().getId()).collect(Collectors.toList()).toString());

        return sw.toString();
    }
}
