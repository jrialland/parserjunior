package net.jr.parser.impl;

import net.jr.common.Symbol;
import net.jr.lexer.CommonTokenTypes;
import net.jr.parser.Grammar;
import net.jr.util.AsciiTableView;
import net.jr.util.TableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class LR0Table {

    private static final Logger LOG = LoggerFactory.getLogger(LR0Table.class);

    private List<Symbol> terminals;

    private List<Symbol> nonTerminals;

    private Map<Integer, Map<Symbol, Action>> tableData;

    private LR0Table(List<Symbol> terminals, List<Symbol> nonTerminals, Map<Integer, Map<Symbol, Action>> tableData) {
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
            Map<Integer, Map<Symbol, Action>> table = getActionTable(grammar, allItemSets);
            List<Symbol> terminals = new ArrayList<>(grammar.getTerminals());
            List<Symbol> nonTerminals = new ArrayList<>(grammar.getNonTerminals());
            LR0Table lr0Table = new LR0Table(terminals, nonTerminals, table);


            Grammar extendedGrammar = makeExtendedGrammar(allItemSets);
            System.out.println(extendedGrammar);

            Map<Symbol, Set<Symbol>> eFollowSets = getFollowSets(extendedGrammar, extendedGrammar.getTargetRule());
            for (Map.Entry<Symbol, Set<Symbol>> entry : eFollowSets.entrySet()) {
                System.out.println(entry.getKey() + " => " + entry.getValue());
            }

            return lr0Table;
        }

        private Map<Symbol, Set<Symbol>> getFollowSets(Grammar grammar, Symbol target) {
            Map<Symbol, FollowSet> map = new HashMap<>();

            //The follow set of a terminal is the empty set
            for (Symbol terminal : grammar.getTerminals()) {
                map.put(terminal, FollowSet.emptySet(terminal));
            }

            //Initialize an empty set for each nonterminal
            for (Symbol s : grammar.getNonTerminals()) {
                map.put(s, new FollowSet(s));
            }

            //Place an End of Input token ($) into the starting rule's follow set.
            map.get(target).setResolution(new HashSet<>(Arrays.asList(CommonTokenTypes.eof())));

            for (Symbol s : grammar.getNonTerminals()) {
                defineFollowSet(map, grammar, s);
            }

            LazySet.resolveAll(map.values());

            return map.values()
                    .stream()
                    .filter(f -> !f.getSubject().isTerminal())
                    .collect(Collectors.toMap(FollowSet::getSubject, FollowSet::getResolution));
        }

        private void defineFollowSet(Map<Symbol, FollowSet> followSets, Grammar grammar, Symbol D) {

            FollowSet followSet = followSets.get(D);

            // Construct for the rule have the form R → a* D b.
            for (Grammar.Rule rule : grammar.getRules()) {

                Symbol R = rule.getTarget();
                List<Symbol> clause = Arrays.asList(rule.getClause());

                //for each occurence of D in the clause
                for (int i = 0, max = clause.size() - 1; i < max; i++) { //minus 1 because if D is the last we are not interested (i.e b must exist)
                    //if (clause.get(i).equals(D)) {
                    if (clause.get(i).equals(D)) {
                        Symbol b = clause.get(i + 1);
                        //Everything in First(b) (except for ε) is added to Follow(D)
                        Set<Symbol> first = new HashSet<>(getFirst(grammar, b));
                        boolean containedEmpty = first.remove(Grammar.Empty);

                        FirstSet firstSet = new FirstSet(b);
                        firstSet.setResolution(first);
                        followSet.add(firstSet);

                        //If First(b) contains ε then everything in Follow(R) is put in Follow(D)
                        if (containedEmpty) {
                            followSet.add(followSets.get(R));
                        }
                    }
                }

                //Finally, if we have a rule R → a* D, then everything in Follow(R) is placed in Follow(D).
                if (!clause.isEmpty() && D.equals(clause.get(clause.size() - 1))) {
                    followSet.add(followSets.get(R));
                }
            }
        }

        private Set<Symbol> getFirst(Grammar grammar, Symbol s) {
            Set<Symbol> set = new HashSet<>();

            // First(terminal) = [terminal]
            if (s.isTerminal()) {
                set.add(s);
                return set;
            }

            for (Grammar.Rule r : grammar.getRules()) {
                if (r.getTarget().equals(s)) {

                    //if the first symbol is a terminal, the set is this terminal
                    Symbol firstTerminal;
                    if (r.getClause().length > 0 && (firstTerminal = r.getClause()[0]).isTerminal()) {
                        set.add(firstTerminal);
                        continue;
                    }

                    //if not, we scan the symbol,
                    boolean brk = false;
                    for (Symbol s2 : r.getClause()) {

                        Set<Symbol> a = getFirst(grammar, s2);
                        boolean containedEmpty = a.remove(Grammar.Empty);
                        set.addAll(a);

                        //if First(x) did not contain ε, we do not need to contine scanning
                        if (!containedEmpty) {
                            brk = true;
                            break;
                        }
                    }

                    //every First(x) contained ε, so we have to add it to the set
                    if (!brk) {
                        set.add(Grammar.Empty);
                    }
                }
            }

            return set;
        }

        private Grammar makeExtendedGrammar(Set<ItemSet> allItemSets) {

            Map<List<?>, ExtendedSymbol> extSyms = new HashMap<>();

            Grammar eGrammar = new Grammar();
            for (ItemSet itemSet : allItemSets) {
                int initialState = itemSet.getId();
                List<Grammar.Rule> rules = itemSet.allItems().filter(item -> item.getPointer() == 0).map(item -> item.getRule()).collect(Collectors.toList());
                for (Grammar.Rule rule : rules) {

                    ItemSet currentItem = itemSet;
                    List<ExtendedSymbol> eClause = new ArrayList<>();
                    for (Symbol s : rule.getClause()) {
                        final int start = currentItem.getId();
                        currentItem = currentItem.getTransitionFor(s);
                        final int end = currentItem.getId();
                        List<?> key = Arrays.asList(start, s, end);
                        ExtendedSymbol extSym = extSyms.computeIfAbsent(key, k -> new ExtendedSymbol(start, s, end));
                        eClause.add(extSym);
                    }
                    final int finalState;
                    ItemSet transition = itemSet.getTransitionFor(rule.getTarget());
                    if (transition == null) {
                        finalState = -1;
                    } else {
                        finalState = transition.getId();
                    }

                    List<?> key = Arrays.asList(initialState, rule.getTarget(), finalState);
                    ExtendedSymbol eTarget = extSyms.computeIfAbsent(key, k -> new ExtendedSymbol(initialState, rule.getTarget(), finalState));
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
