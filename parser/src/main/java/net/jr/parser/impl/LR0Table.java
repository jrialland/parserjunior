package net.jr.parser.impl;

import net.jr.common.Symbol;
import net.jr.lexer.Lexeme;
import net.jr.parser.Grammar;
import net.jr.util.AsciiTableView;
import net.jr.util.TableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class LR0Table {

    private static final Logger LOG = LoggerFactory.getLogger(LR0Table.class);

    private List<Lexeme> terminals;

    private List<Symbol> nonTerminals;

    private Map<Integer, Map<Symbol, Action>> tableData;

    private LR0Table(List<Lexeme> terminals, List<Symbol> nonTerminals, Map<Integer, Map<Symbol, Action>> tableData) {
        this.terminals = terminals;
        this.nonTerminals = nonTerminals;
        this.tableData = tableData;
    }

    private void feedFirstLine(TableModel<String> tableModel) {
        int i = 0;
        for (; i < terminals.size(); i++) {
            tableModel.setData(i + 1, 0, terminals.get(i).toString());
        }
        int s = i, max = i + nonTerminals.size();
        for (; i < max; i++) {
            tableModel.setData(i + 1, 0, nonTerminals.get(i - s).toString());
        }
    }

    @Override
    public String toString() {
        TableModel<String> tableModel = new TableModel<>();
        feedFirstLine(tableModel);
        List<Symbol> allSymbols = new ArrayList<>(terminals);
        allSymbols.addAll(nonTerminals);

        for (int i = 0; i < tableData.size(); i++) {
            Map<Symbol, Action> actions = tableData.get(i);
            tableModel.setData(0, i + 1, Integer.toString(i));
            int x = 1;
            for (Symbol symbol : allSymbols) {
                Action action = actions.get(symbol);
                String sAction = action == null ? "" : Integer.toString(action.getActionParameter());
                tableModel.setData(x++, i + 1, sAction);
            }
        }

        return AsciiTableView.tableToString(tableModel);
    }

    private static final Logger getLog() {
        return LOG;
    }

    public static class Builder {

        public LR0Table build(Grammar grammar, Symbol target) {
            Grammar.Rule startingRule = grammar.getRules().stream().filter(r -> r.getTarget().equals(target)).findFirst().get();
            ItemSet i0 = getFirstItemSet(grammar, startingRule);
            Set<ItemSet> allItemSets = getAllItemSets(grammar, i0);

            if (getLog().isDebugEnabled()) {
                getLog().debug(String.format("There are %d itemSets", allItemSets.size()));
                for (ItemSet itemSet : allItemSets) {
                    getLog().debug(itemSet.toString());
                }
            }

            Map<Integer, Map<Symbol, Action>> table = getActionTable(grammar, allItemSets);
            List<Lexeme> terminals = new ArrayList<>(grammar.getTerminals());
            List<Symbol> nonTerminals = new ArrayList<>(grammar.getNonTerminals());
            LR0Table lr0Table = new LR0Table(terminals, nonTerminals, table);


            //extended grammar
            Grammar extendedGrammar = makeExtendedGrammar(allItemSets);
            System.out.println(extendedGrammar);

            return lr0Table;
        }

        private Grammar makeExtendedGrammar(Set<ItemSet> allItemSets) {
            Grammar eGrammar = new Grammar();
            for (ItemSet itemSet : allItemSets) {
                int initialState = itemSet.getId();
                List<Grammar.Rule> rules = itemSet.allItems().filter(item -> item.getPointer() == 0).map(item -> item.getRule()).collect(Collectors.toList());
                for (Grammar.Rule rule : rules) {
                    int start = 0, end = -1;
                    ItemSet currentItem = itemSet;
                    List<ExtendedSymbol> eClause = new ArrayList<>();
                    for (Symbol s : rule.getClause()) {
                        start = currentItem.getId();
                        currentItem = currentItem.getTransitionFor(s);
                        end = currentItem.getId();
                        eClause.add(new ExtendedSymbol(start, s, end));
                    }
                    final int finalState;
                    ItemSet transition = itemSet.getTransitionFor(rule.getTarget());
                    if (transition == null) {
                        finalState = -1;
                    } else {
                        finalState = transition.getId();
                    }

                    ExtendedSymbol eTarget = new ExtendedSymbol(initialState, rule.getTarget(), finalState);
                    eGrammar.addRule(eTarget, eClause);
                }
            }
            return eGrammar;
        }


        private Map<Integer, Map<Symbol, Action>> getActionTable(Grammar grammar, Set<ItemSet> allItemSets) {
            Map<Integer, Map<Symbol, Action>> table = new TreeMap<>();
            for (ItemSet itemSet : allItemSets) {
                int currentState = itemSet.getId();
                Map<Symbol, Action> row = new HashMap<>();
                table.put(currentState, row);
                for (Symbol symbol : grammar.getSymbols()) {
                    ItemSet targetItemSet = itemSet.getTransitionFor(symbol);
                    if (targetItemSet != null) {
                        row.put(symbol, new Action(ActionType.Shift, targetItemSet.getId()));
                    }
                }
            }
            return table;
        }

        /**
         * Once we have the item set for the starting rule of the grammar,
         * generate all the other sets so we can get all LR(0) transitions
         *
         * @param grammar
         * @param i0
         * @return
         */
        private Set<ItemSet> getAllItemSets(Grammar grammar, ItemSet i0) {
            Set<ItemSet> set = new HashSet<>();
            set.add(i0);

            Map<Set<Item>, ItemSet> knownKernels = new HashMap<>();
            knownKernels.put(i0.getKernel(), i0);

            Stack<ItemSet> stack = new Stack<>();
            stack.add(i0);

            int i = 1;
            while (!stack.isEmpty()) {
                ItemSet currentItemSet = stack.pop();
                for (Symbol symbol : currentItemSet.getPossibleNextSymbols()) {

                    Set<Item> newKernel = new HashSet<>();
                    for (Item item : currentItemSet.getItemsThatExpect(symbol)) {
                        Item nextItem = item.shift();
                        newKernel.add(nextItem);
                    }

                    if (!newKernel.isEmpty()) {
                        ItemSet newItemSet = knownKernels.get(newKernel);
                        if (newItemSet == null) {
                            newItemSet = new ItemSet(newKernel, extendItemSetKernel(grammar, newKernel));
                            knownKernels.put(newKernel, newItemSet);
                            newItemSet.setId(i++);
                            set.add(newItemSet);
                            stack.push(newItemSet);
                        }
                        currentItemSet.addTransition(symbol, newItemSet);
                    }
                }
            }
            return set;
        }

        /**
         * The first item set, I0 begins with the starting rule.
         */
        private ItemSet getFirstItemSet(Grammar grammar, Grammar.Rule startingRule) {
            Item firstItem = new Item(startingRule, 0);
            Set<Item> kernel = new HashSet<>();
            kernel.add(firstItem);
            ItemSet i0 = new ItemSet(kernel, extendItemSetKernel(grammar, kernel));
            i0.setId(0);
            return i0;
        }

        /**
         * Given the kernel of an item set, derivate it in order to get the rest of the closure.
         *
         * @param grammar
         * @param kernel
         * @return
         */
        private Set<Item> extendItemSetKernel(Grammar grammar, Set<Item> kernel) {
            Set<Item> set = new HashSet<>();
            Stack<Item> stack = new Stack<>();
            stack.addAll(kernel);
            while (!stack.isEmpty()) {
                Item currentItem = stack.pop();
                Symbol expected = currentItem.getExpectedSymbol();
                if (expected != null) {
                    //find all the rules starting with 'expected'
                    grammar.getRules().stream().filter(r -> r.getTarget().equals(expected)).forEach(r -> {
                        Item i = new Item(r, 0);
                        if (!set.contains(i)) {
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
