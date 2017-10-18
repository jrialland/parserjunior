package net.jr.parser.impl;

import net.jr.common.Symbol;
import net.jr.parser.Grammar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class LR0Table {

    private static final Logger LOG = LoggerFactory.getLogger(LR0Table.class);

    private LR0Table() {

    }

    private static final Logger getLog() {
        return LOG;
    }

    public static class Builder {

        public LR0Table build(Grammar grammar, Symbol target) {
            Grammar.Rule startingRule = grammar.getRules().stream().filter(r->r.getTarget().equals(target)).findFirst().get();
            getLog().debug("Starting Rule : " + startingRule);
            ItemSet i0 = getFirstItemSet(grammar, startingRule);
            Set<ItemSet> allItemSets = getAllItemSets(grammar, i0);
            for(ItemSet i : allItemSets) {
                System.out.println(i.toString());
            }

            return new LR0Table();
        }

        private Set<ItemSet> getAllItemSets(Grammar grammar, ItemSet i0) {
            Set<ItemSet> set = new HashSet<>();
            Stack<ItemSet> stack = new Stack<>();
            stack.add(i0);
            int i=1;
            while(!stack.isEmpty()) {
                ItemSet currentItemSet = stack.pop();
                set.add(currentItemSet);
                for(Symbol symbol : currentItemSet.getPossibleNextSymbols()) {
                    Set<Item> newKernel = new HashSet<>();
                    for(Item item : currentItemSet.getItemsThatExpect(symbol)) {
                        Item nextItem = new Item(item.getRule(), item.getPointer()+1);//shifted item
                        newKernel.add(nextItem);
                    }
                    if(!newKernel.isEmpty()) {
                        ItemSet newItemSet = new ItemSet((i++), newKernel, extendItemSetKernel(grammar, newKernel));
                        if(currentItemSet.equals(newItemSet)) {
                           currentItemSet.addTransition(symbol, currentItemSet);
                        } else {
                            currentItemSet.addTransition(symbol, newItemSet);
                        }
                        if(set.add(newItemSet)) {
                            stack.push(newItemSet);
                        }
                    }
                }
            }
            return set;
        }

        private ItemSet getFirstItemSet(Grammar grammar, Grammar.Rule startingRule) {
            Item firstItem = new Item(startingRule, 0);
            Set<Item> kernel = new HashSet<>();
            kernel.add(firstItem);
            return new ItemSet(0, kernel, extendItemSetKernel(grammar, kernel));
        }

        private Set<Item> extendItemSetKernel(Grammar grammar, Set<Item> kernel) {
            Set<Item> set = new HashSet<>();
            Stack<Item> stack = new Stack<>();
            stack.addAll(kernel);
            while(!stack.isEmpty()) {
                Item currentItem = stack.pop();
                Symbol expected = currentItem.getExpectedSymbol();
                if(expected != null) {
                    //find all the rules starting with 'expected'
                    grammar.getRules().stream().filter(r->r.getTarget().equals(expected)).forEach(r -> {
                        Item i = new Item(r, 0);
                        if(!set.contains(i)) {
                            set.add(i);
                            stack.push(i);
                        }
                    });
                }
            }
            set.removeAll(kernel);
            return set;
        }

    }
}
