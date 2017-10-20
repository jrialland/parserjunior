package net.jr.parser.impl;

import net.jr.common.Symbol;
import net.jr.lexer.Lexemes;
import net.jr.lexer.Lexeme;
import net.jr.parser.Grammar;
import net.jr.parser.Rule;
import net.jr.util.AsciiTableView;
import net.jr.util.TableModel;

import java.util.*;
import java.util.stream.Collectors;

/**
 * action table is indexed by a state of the parser and a terminal (including a special terminal ᵉᵒᶠ ({@link Lexemes#eof()}) that indicates the end of the input stream) and contains three types of actions:
 * <ul>
 * <li>shift, which is written as 'sn' and indicates that the next state is n</li>
 * <li>reduce, which is written as 'rm' and indicates that a reduction with grammar rule m should be performed</li>
 * <li>accept, which is written as 'acc' and indicates that the parser accepts the string in the input stream.</li>
 * </ul>
 */
public class ActionTable {

    private Map<Integer, Map<Symbol, Action>> data = new TreeMap<>();

    private List<Symbol> terminals;

    private List<Symbol> nonTerminals;

    private ActionTable(Set<Symbol> terminals, Set<Symbol> nonTerminals) {
        this.terminals = new ArrayList<>(terminals);
        if (!terminals.contains(Lexemes.eof())) {
            this.terminals.add(Lexemes.eof());
        }
        Collections.sort(this.terminals, Comparator.comparing(Symbol::toString));

        this.nonTerminals = new ArrayList<>(nonTerminals);
        Collections.sort(this.nonTerminals, Comparator.comparing(Symbol::toString));
    }

    private void setAction(int state, Symbol symbol, Action action) {
        data.computeIfAbsent(state, k -> new HashMap<>()).put(symbol, action);
    }

    Action getAction(int state, Lexeme symbol) {
        return _getAction(state, symbol);
    }

    int getNextState(int currentState, Symbol symbol) {
        Action gotoAction = _getAction(currentState, symbol);
        return gotoAction.getActionParameter();
    }

    private Action _getAction(int state, Symbol symbol) {
        Map<Symbol, Action> row = data.get(state);
        if (row == null) {
            throw new IllegalStateException(String.format("No such state (%d)", state));
        }
        return row.get(symbol);
    }



    Set<Lexeme> getExpectedLexemes(int state) {
        Map<Symbol, Action> row = data.get(state);
        return row.values().stream().map(s -> (Lexeme) s).collect(Collectors.toSet());
    }

    private int getColumnFor(Symbol symbol) {
        if (symbol.isTerminal()) {
            return terminals.indexOf(symbol);
        } else {
            return terminals.size() + nonTerminals.indexOf(symbol);
        }
    }

    private static String actionToString(Action action) {
        final String sParam = Integer.toString(action.getActionParameter());
        switch (action.getActionType()) {
            case Accept:
                return "acc";
            case Fail:
                return "";
            case Goto:
                return Integer.toString(action.getActionParameter());
            case Shift:
                return "s" + sParam;
            case Reduce:
                return "r" + sParam;
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public String toString() {
        TableModel<String> tm = new TableModel<>();
        for (Map.Entry<Integer, Map<Symbol, Action>> rowEntry : data.entrySet()) {
            int state = rowEntry.getKey();
            for (Map.Entry<Symbol, Action> e : rowEntry.getValue().entrySet()) {
                Symbol s = e.getKey();
                Action action = e.getValue();
                tm.setData(getColumnFor(s), state, actionToString(action));
            }
        }

        //make room for the labels
        tm.moveDataBy(1, 1);

        //row labels
        for (Map.Entry<Integer, Map<Symbol, Action>> rowEntry : data.entrySet()) {
            int state = rowEntry.getKey();
            tm.setData(0, 1 + state, Integer.toString(state));
        }

        //column labels
        int col = 1;
        for (Symbol term : terminals) {
            tm.setData(col++, 0, term.toString());
        }
        for (Symbol term : nonTerminals) {
            tm.setData(col++, 0, term.toString());
        }

        return new AsciiTableView(4, 100).tableToString(tm);
    }

    public static ActionTable lalr1(Grammar grammar, Rule startRule) {
        return new LALR1Builder().build(grammar, startRule);
    }

    private static class LALR1Builder {

        public ActionTable build(Grammar grammar, Rule startRule) {

            //Syntax Analysis Goal: Item Sets
            ItemSet i0 = getFirstItemSet(grammar, startRule);
            Set<ItemSet> allItemSets = getAllItemSets(grammar, i0);

            //Syntax Analysis Goal: Translation Table
            Map<Integer, Map<Symbol, Integer>> translationTable = getTranslationTable(grammar, allItemSets);

            // Syntax Analysis Goal: Extended Grammar


            //Syntax Analysis Goal: Action and Goto Table
            ActionTable actionTable = new ActionTable(grammar.getTerminals(), grammar.getNonTerminals());
            initializeTable(actionTable, startRule, allItemSets);
            initializeShiftsAndGotos(actionTable, translationTable);
            initializeReductions(actionTable, grammar, startRule, allItemSets);
            return actionTable;
        }

        private void initializeReductions(ActionTable table, Grammar grammar, Rule startRule, Set<ItemSet> itemSets) {


            Grammar extendedGrammar = makeExtendedGrammar(itemSets);
            // Syntax Analysis Goal: FOLLOW Sets
            Map<Symbol, Set<Symbol>> followSets = getFollowSets(extendedGrammar, extendedGrammar.getTargetSymbol());
            //build a list of rules and and follow sets

            Map<Rule, Set<Symbol>> step1 = new HashMap<>();
            for (Rule eRule : extendedGrammar.getRules()) {
                Set<Symbol> followSet = followSets.get(eRule.getTarget())
                        .stream()
                        .map(s -> (s instanceof ExtendedSymbol) ? ((ExtendedSymbol) s).getSymbol() : s)
                        .collect(Collectors.toSet());
                step1.put(eRule, followSet);
            }

            for (Rule rule : grammar.getRules()) {
                for (int i = 0; i < itemSets.size(); i++) {
                    final int state = i;
                    Set<Symbol> mergedFollowSet = step1.keySet().stream()
                            .map(r -> (ExtendedRule) r)
                            .filter(r -> r.isExtensionOf(rule) && r.getFinalState() == state)
                            .map(r -> step1.get(r))
                            .flatMap(Set::stream)
                            .collect(Collectors.toSet());

                    if (!mergedFollowSet.isEmpty()) {

                        //ignore the reduction involving state 0 & the starting rule
                        if (state == 0 && rule.equals(startRule)) {
                            continue;
                        }

                        for (Symbol rSymbol : mergedFollowSet) {

                            final Action action;
                            if (rule.getId() == 0) {
                                action = new Action(ActionType.Accept, 0);
                            } else {
                                action = new Action(ActionType.Reduce, rule.getId());
                            }

                            table.setAction(state, rSymbol, action);

                        }
                    }
                }
            }

        }


        /**
         * Step 1 - Initialize
         * <p>
         * Add a column for the end of input, labelled $.
         * Place an "accept" in the $ column whenever the item set
         * contains an item where the pointer is at the end of the starting rule (in our example "S → N •").
         * </p>
         */
        private void initializeTable(ActionTable table, Rule startingRule, Set<ItemSet> itemSets) {
            final Symbol eof = Lexemes.eof();
            final Action accept = new Action(ActionType.Accept, 0);
            Item allParsed = new Item(startingRule, startingRule.getClause().length);
            itemSets.stream()
                    .filter(itemSet -> itemSet.allItems().filter(item -> item.equals(allParsed)).findAny().isPresent())
                    .forEach(itemSet -> table.setAction(itemSet.getId(), eof, accept));
        }

        /**
         * Step 2 - Gotos + Step 3 - Shifts
         * <p>
         * Directly copy the Translation Table's nonterminal columns as GOTOs.
         * </p>
         *
         * @param table
         * @param translationTable
         */
        private void initializeShiftsAndGotos(ActionTable table, Map<Integer, Map<Symbol, Integer>> translationTable) {
            for (Map.Entry<Integer, Map<Symbol, Integer>> tEntry : translationTable.entrySet()) {
                int state = tEntry.getKey();
                for (Map.Entry<Symbol, Integer> entry : tEntry.getValue().entrySet()) {
                    Symbol s = entry.getKey();
                    if (s.isTerminal()) {
                        table.setAction(state, s, new Action(ActionType.Shift, entry.getValue()));
                    } else {
                        table.setAction(state, s, new Action(ActionType.Goto, entry.getValue()));
                    }
                }
            }
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
            map.get(target).setResolution(new HashSet<>(Arrays.asList(Lexemes.eof())));

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
            for (Rule rule : grammar.getRules()) {

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

            for (Rule r : grammar.getRules()) {
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
            int idCounter = 0;
            for (ItemSet itemSet : allItemSets) {
                int initialState = itemSet.getId();
                List<Rule> rules = itemSet.allItems().filter(item -> item.getPointer() == 0).map(item -> item.getRule()).collect(Collectors.toList());
                for (Rule rule : rules) {

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
                    eGrammar.addRule(new ExtendedRule(idCounter++, rule, eTarget, eClause.toArray(new ExtendedSymbol[]{})));
                }
            }
            return eGrammar;
        }


        private Map<Integer, Map<Symbol, Integer>> getTranslationTable(Grammar grammar, Set<ItemSet> allItemSets) {
            Map<Integer, Map<Symbol, Integer>> table = new TreeMap<>();
            for (ItemSet itemSet : allItemSets) {
                int currentState = itemSet.getId();
                Map<Symbol, Integer> row = new HashMap<>();
                table.put(currentState, row);
                for (Symbol symbol : grammar.getSymbols()) {
                    ItemSet targetItemSet = itemSet.getTransitionFor(symbol);
                    if (targetItemSet != null) {
                        row.put(symbol, targetItemSet.getId());
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
        private ItemSet getFirstItemSet(Grammar grammar, Rule startingRule) {
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
