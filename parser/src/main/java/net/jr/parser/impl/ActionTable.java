package net.jr.parser.impl;

import net.jr.common.Symbol;
import net.jr.lexer.Lexeme;
import net.jr.lexer.Lexemes;
import net.jr.marshalling.MarshallingCapable;
import net.jr.marshalling.MarshallingUtil;
import net.jr.parser.Grammar;
import net.jr.parser.Rule;
import net.jr.util.table.AsciiTableView;
import net.jr.util.table.TableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringWriter;
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
public class ActionTable implements MarshallingCapable {

    private static Logger Logger = LoggerFactory.getLogger(ActionTable.class);

    private static final Logger getLog() {
        return Logger;
    }

    private Map<Integer, Map<Symbol, Action>> data = new TreeMap<>();

    private List<Symbol> terminals;

    private List<Symbol> nonTerminals;

    public int getStatesCount() {
        return data.size();
    }

    @Override
    public void marshall(DataOutputStream dataOutputStream) throws IOException {
        MarshallingUtil.marshall(terminals, dataOutputStream);
        MarshallingUtil.marshall(nonTerminals, dataOutputStream);
        MarshallingUtil.marshall(data, dataOutputStream);
    }

    @SuppressWarnings("unused")
    public static ActionTable unMarshall(DataInputStream dataInputStream) throws IOException {
        ActionTable actionTable = new ActionTable();
        actionTable.terminals = MarshallingUtil.unmarshall(dataInputStream);
        actionTable.nonTerminals = MarshallingUtil.unmarshall(dataInputStream);
        actionTable.data = MarshallingUtil.unmarshall(dataInputStream);
        return actionTable;
    }

    private ActionTable() {
    }

    private void onInitialized() {
        Set<Symbol> allSymbols = data.values().stream().map(m -> m.keySet()).reduce(new HashSet<>(), (acc, s) -> {
            acc.addAll(s);
            return acc;
        });
        terminals = allSymbols.stream().filter(s -> s.isTerminal()).collect(Collectors.toList());
        terminals.sort(Comparator.comparing(Symbol::toString));
        nonTerminals = allSymbols.stream().filter(s -> !s.isTerminal()).collect(Collectors.toList());
        nonTerminals.sort(Comparator.comparing(Symbol::toString));
    }

    private void setAction(int state, Symbol symbol, Action action, boolean allowReplace) {
        Map<Symbol, Action> row = data.computeIfAbsent(state, k -> new HashMap<>());
        Action oldAction = row.put(symbol, action);

        if (!allowReplace && oldAction != null && !oldAction.equals(action)) {
            StringWriter sw = new StringWriter();
            sw.append("Unresolved " + oldAction.getActionType() + "/" + action.getActionType() + " conflict");
            sw.append("    For state : " + state);
            sw.append("    For symbol : " + symbol);
            sw.append("    Action 1 : " + oldAction.toString());
            sw.append("    Action 2 : " + action.toString());
            throw new IllegalStateException(sw.toString());
        }
    }

    public Action getAction(int state, Symbol symbol) {
        return _getAction(state, symbol);
    }

    int getNextState(int currentState, Symbol symbol) {
        Action gotoAction = _getAction(currentState, symbol);
        if (gotoAction == null) {
            throw new IllegalStateException(String.format("No GOTO Action for state '%d', Symbol '%s'", currentState, symbol));
        }
        return gotoAction.getActionParameter();
    }

    private Action _getAction(int state, Symbol symbol) {
        Map<Symbol, Action> row = data.get(state);
        if (row == null) {
            throw new IllegalStateException(String.format("No such state (%d)", state));
        }
        return row.get(symbol);
    }

    private Action getActionNoCheck(int state, Symbol s) {
        Map<Symbol, Action> row = data.get(state);
        return row == null ? null : row.get(s);
    }

    Set<Lexeme> getExpectedLexemes(int state) {
        Map<Symbol, Action> row = data.get(state);
        return row.keySet().stream()
                .filter(s -> s.isTerminal())
                .map(s -> (Lexeme) s).collect(Collectors.toSet());
    }

    private int getColumnFor(Symbol symbol) {
        if (symbol.isTerminal()) {
            return getTerminals().indexOf(symbol);
        } else {
            return getTerminals().size() + getNonTerminals().indexOf(symbol);
        }
    }

    public List<Symbol> getTerminals() {
        return terminals;
    }

    public List<Symbol> getNonTerminals() {
        return nonTerminals;
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

    public static ActionTable lalr1(Grammar grammar) {
        return new LALR1Builder().build(grammar);
    }

    static class LALR1Builder {

        public ActionTable build(Grammar grammar) {

            Set<Rule> targetRules = grammar.getRulesTargeting(grammar.getTargetSymbol());
            if (targetRules.size() != 1) {
                throw new IllegalStateException("Illegal target rule specification (required : only one rule for the target symbol)");
            }
            Rule startRule = targetRules.iterator().next();

            getLog().trace("Building action Table for : " + grammar.toString());
            getLog().trace("Starting Rule is : " + startRule);

            //Syntax Analysis Goal: Item Sets

            Set<ItemSet> allItemSets = getAllItemSets(grammar);

            //Syntax Analysis Goal: Translation Table
            Map<Integer, Map<Symbol, Integer>> translationTable = getTranslationTable(grammar, allItemSets);

            //Syntax Analysis Goal: Action and Goto Table
            ActionTable actionTable = new ActionTable();

            initializeShiftsAndGotos(actionTable, translationTable);
            initializeReductions(grammar, actionTable, startRule, allItemSets);
            initializeAccept(actionTable, startRule, allItemSets);

            actionTable.onInitialized();

            return actionTable;
        }

        /**
         * Add all the reduce actions to the table.
         *
         * @param grammar
         * @param table
         * @param startRule
         * @param itemSets
         */
        void initializeReductions(Grammar grammar, ActionTable table, Rule startRule, Set<ItemSet> itemSets) {

            Grammar extendedGrammar = makeExtendedGrammar(startRule, itemSets);

            // Syntax Analysis Goal: FOLLOW Sets
            Map<Symbol, Set<? extends Symbol>> followSets = getFollowSets(extendedGrammar);

            //build a list of rules and and follow sets
            List<PreMergeReduction> step1 = new ArrayList<>();
            for (Rule eRule : extendedGrammar.getRules()) {
                Set<Symbol> followSet = followSets.get(eRule.getTarget())
                        .stream()
                        .map(s -> (s instanceof ExtendedSymbol) ? ((ExtendedSymbol) s).getSymbol() : s)
                        .collect(Collectors.toSet());

                step1.add(new PreMergeReduction((ExtendedRule) eRule, followSet));
            }

            //merging some rules
            Set<MergedReduction> step2 = new HashSet<>();
            for (PreMergeReduction pm : step1) {
                List<PreMergeReduction> matching = step1.stream().filter(r -> r.matches(pm)).collect(Collectors.toList());
                if (matching.size() == 1) {
                    MergedReduction merged = new MergedReduction(pm.getBaseRule(), pm.getFinalState(), pm.getFollowSet());
                    step2.add(merged);
                } else {
                    Set<Symbol> newFollowSet = new HashSet<>();
                    for (PreMergeReduction m : matching) {
                        newFollowSet.addAll(m.getFollowSet());
                    }
                    MergedReduction merged = new MergedReduction(pm.getBaseRule(), pm.getFinalState(), newFollowSet);
                    step2.add(merged);
                }
            }

            //feed the action table with reductions
            for (MergedReduction merged : step2) {
                int ruleId = merged.getRule().getId();
                for (Symbol s : merged.getFollowSet()) {
                    int state = merged.getFinalState();
                    //add a reduce action to the table
                    Action actionToInsert = new Action(ActionType.Reduce, ruleId);
                    Action existingAction = table.getActionNoCheck(state, s);
                    if (existingAction != null) {
                        table.setAction(state, s, resolveConflict(grammar, merged.getRule(), s, existingAction, actionToInsert), true);
                    } else {
                        table.setAction(state, s, actionToInsert, false);
                    }
                }
            }
        }

        /**
         * When the action table already has an action (accept oneOf shift) for a given state/lexeme,
         * try to apply precedence rules to arbitrate
         *
         * @param grammar      the grammar
         * @param rule         the rule that is reduced
         * @param existing     a shift oneOf accept oneOf reduce
         * @param reduceAction a reduce action
         * @return
         */
        private Action resolveConflict(Grammar grammar, Rule rule, Symbol symbol, Action existing, Action reduceAction) {
            switch (existing.getActionType()) {

                case Accept:
                    //accept always wins
                    return existing;

                case Shift:
                    // shift/reduce conflict !
                    ActionType preference = grammar.getConflictResolutionHint(rule, symbol);
                    if (preference != null) {
                        switch (preference) {
                            case Fail:
                                return null;
                            case Shift:
                                return existing;
                            case Reduce:
                                return reduceAction;
                            default:
                                throw new IllegalArgumentException(preference.toString());
                        }
                    } else {

                        //choose the shift by default
                        return existing;

                    }
                case Reduce:
                    return existing;

                default:
                    throw new IllegalStateException();
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
        void initializeAccept(ActionTable table, Rule startingRule, Set<ItemSet> itemSets) {
            final Symbol eof = Lexemes.eof();
            final Action accept = new Action(ActionType.Accept, 0);
            Item allParsed = new Item(startingRule, startingRule.getClause().length);
            itemSets.stream()
                    .filter(itemSet -> itemSet.allItems().filter(item -> item.equals(allParsed)).findAny().isPresent())
                    .forEach(itemSet -> table.setAction(itemSet.getId(), eof, accept, true));
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
        void initializeShiftsAndGotos(ActionTable table, Map<Integer, Map<Symbol, Integer>> translationTable) {
            for (Map.Entry<Integer, Map<Symbol, Integer>> tEntry : translationTable.entrySet()) {
                int state = tEntry.getKey();
                for (Map.Entry<Symbol, Integer> entry : tEntry.getValue().entrySet()) {
                    Symbol s = entry.getKey();
                    if (s.isTerminal()) {
                        table.setAction(state, s, new Action(ActionType.Shift, entry.getValue()), false);
                    } else {
                        table.setAction(state, s, new Action(ActionType.Goto, entry.getValue()), false);
                    }
                }
            }
        }

        /**
         * Compte the FOLLOW sets for all the symbols of a grammar
         *
         * @param grammar the grammar
         * @return the FOLLOW sets for all the symbols of a grammar
         */
        Map<Symbol, Set<? extends Symbol>> getFollowSets(Grammar grammar) {


            //The follow set of a terminal is always empty
            Map<Symbol, FollowSet> map = grammar.getSymbols().stream()
                    .filter(s -> s.isTerminal())
                    .collect(Collectors.toMap(s -> s, s -> FollowSet.emptySet(s)));

            //Initialize an new set for each nonterminal
            for (Symbol s : grammar.getNonTerminals()) {
                map.put(s, new FollowSet(s));
            }

            //Place an End of Input token ($) into the starting rule's follow set.
            map.get(grammar.getTargetSymbol()).setResolution(new HashSet<>(Arrays.asList(Lexemes.eof())));

            for (Symbol s : grammar.getNonTerminals()) {
                defineFollowSet(map, grammar, s);
            }

            LazySet.resolveAll(map);

            return map.values()
                    .stream()
                    .filter(f -> !f.getSubject().isTerminal())
                    .collect(Collectors.toMap(FollowSet::getSubject, FollowSet::getResolution));
        }

        /**
         * computes FOLLOW(D).
         *
         * @param followSets map of all follow sets,
         * @param grammar    The grammar
         * @param D          the symbol we compute the set for
         */
        void defineFollowSet(Map<Symbol, FollowSet> followSets, Grammar grammar, Symbol D) {

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
                        Set<Symbol> first = new HashSet<>(getFirstSet(grammar, b));
                        boolean containedEmpty = first.remove(Lexemes.empty());

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

        /**
         * computes FIRST(symbol)
         *
         * @param grammar
         * @param s
         * @return FIRST(symbol)
         */
        Set<Symbol> getFirstSet(Grammar grammar, Symbol s) {

            Set<Symbol> set = new HashSet<>();

            if (s.isTerminal()) {
                // First(terminal) = [terminal]
                if (s instanceof ExtendedSymbol) {
                    set.add(((ExtendedSymbol) s).getSymbol());
                } else {
                    set.add(s);
                }

            } else {

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
                            if (!s.equals(s2)) {
                                Set<Symbol> a = getFirstSet(grammar, s2);
                                boolean containedEmpty = a.remove(Lexemes.empty());
                                set.addAll(a);
                                //if First(x) did not contain ε, we do not need to contine scanning
                                if (!containedEmpty) {
                                    brk = true;
                                    break;
                                }
                            }
                        }

                        //every First(x) contained ε, so we have to add it to the set
                        if (!brk) {
                            set.add(Lexemes.empty());
                        }
                    }
                }
            }

            return set.stream().map(sym -> {
                if (sym instanceof ExtendedSymbol) {
                    return ((ExtendedSymbol) sym).getSymbol();
                } else {
                    return sym;
                }
            }).collect(Collectors.toSet());

        }

        /**
         * Constructs 'extended' grammar, ie a more precise grammar than the original one, deduced from
         * the item sets that have been computed in the previous step
         *
         * @param targetRule
         * @param allItemSets
         * @return
         */
        Grammar makeExtendedGrammar(Rule targetRule, Set<ItemSet> allItemSets) {
            Set<ExtendedSymbol> extendedSymbols = new HashSet<>();
            Grammar eGrammar = new Grammar();

            int[] counter = new int[]{0};

            for (ItemSet itemSet : allItemSets) {
                int initialState = itemSet.getId();
                itemSet.allItems()
                        .filter(item -> item.getPointer() == 0)
                        .map(item -> item.getRule())
                        .forEach(rule -> {

                            ItemSet currentItem = itemSet;
                            List<ExtendedSymbol> eClause = new ArrayList<>();

                            for (Symbol s : rule.getClause()) {
                                final int start = currentItem.getId();
                                currentItem = currentItem.getTransitionFor(s);
                                final int end = currentItem.getId();
                                ExtendedSymbol extSym = new ExtendedSymbol(start, s, end);
                                if(!extendedSymbols.contains(extSym)) {
                                    extendedSymbols.add(extSym);
                                }
                                eClause.add(extSym);
                            }

                            final int finalState;

                            ItemSet transition = itemSet.getTransitionFor(rule.getTarget());
                            if (transition == null) {
                                finalState = -1;
                            } else {
                                finalState = transition.getId();
                            }

                            ExtendedSymbol eTarget = new ExtendedSymbol(initialState, rule.getTarget(), finalState);
                            if(!extendedSymbols.contains(eTarget)) {
                                extendedSymbols.add(eTarget);
                            }
                            eGrammar.addRule(new ExtendedRule(counter[0]++, rule, eTarget, eClause.toArray(new ExtendedSymbol[]{})));
                        });
            }

            ExtendedRule eTargetRule = eGrammar.getRules().stream().map(r -> (ExtendedRule) r).filter(r -> ((ExtendedSymbol) r.getTarget()).getSymbol().equals(targetRule.getTarget())).findFirst().get();
            eGrammar.setTargetRule(eTargetRule);
            return eGrammar;
        }

        /**
         * map each state to the states that can be reached for a given symbol.
         *
         * @param grammar     a grammar
         * @param allItemSets the itemSets for this grammar
         * @return
         */
        Map<Integer, Map<Symbol, Integer>> getTranslationTable(Grammar grammar, Set<ItemSet> allItemSets) {
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
         * @return
         */
        Set<ItemSet> getAllItemSets(Grammar grammar) {
            ItemSet i0 = getFirstItemSet(grammar);

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
        ItemSet getFirstItemSet(Grammar grammar) {
            Rule startingRule = grammar.getRulesTargeting(grammar.getTargetSymbol()).iterator().next();
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
        Set<Item> extendItemSetKernel(Grammar grammar, Set<Item> kernel) {
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
