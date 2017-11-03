package net.jr.parser;


import net.jr.common.Symbol;
import net.jr.lexer.Lexeme;
import net.jr.parser.ast.AstNode;
import net.jr.parser.impl.ActionTable;
import net.jr.parser.impl.ActionType;
import net.jr.parser.impl.BaseRule;
import net.jr.parser.impl.LRParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * https://web.cs.dal.ca/~sjackson/lalr1.html
 * http://jsmachines.sourceforge.net/machines/lalr1.html
 * https://www.codeproject.com/articles/252399/lalr-parse-table-generation-in-csharp
 */
public class Grammar {

    private static final Logger LOGGER = LoggerFactory.getLogger(Grammar.class);

    private static final Logger getLog() {
        return LOGGER;
    }

    public static final Lexeme Empty = new Lexeme() {

        @Override
        public String toString() {
            return "ε";
        }
    };

    private String name;

    private Set<Rule> rules = new HashSet<>();

    private Map<Symbol, Integer> precedenceLevels = new HashMap<>();

    private Symbol targetSymbol;

    public Grammar() {
        this(null);
    }

    public Grammar(String name) {
        this.name = name;
    }

    public Set<Rule> getRules() {
        return rules;
    }

    public interface ComponentsSpecifier {
        ComponentsSpecifier def(Symbol... symbols);
    }

    public ComponentsSpecifier target(Symbol symbol) {
        return new ComponentsSpecifier() {
            @Override
            public ComponentsSpecifier def(Symbol... symbols) {
                addRule(symbol, symbols);
                return this;
            }
        };
    }

    public interface RuleSpecifier {

        RuleSpecifier withAction(Consumer<AstNode> consumer);

        RuleSpecifier withPrecedenceLevel(int level);

        RuleSpecifier withName(String name);

        RuleSpecifier preferShiftOverReduce();

        RuleSpecifier preferReduceOverShift();

        RuleSpecifier withAssociativity(Associativity associativity);

        Rule get();
    }

    public void addRule(Rule rule) {
        if (rules.isEmpty()) {
            targetSymbol = rule.getTarget();
        }
        rules.add(rule);
    }

    public RuleSpecifier addEmptyRule(Symbol target) {
        return addRule(target);
    }

    public RuleSpecifier addRule(Symbol target, Collection<Symbol> clause) {
        return addRule(target, clause.toArray(new Symbol[]{}));
    }

    public RuleSpecifier addRule(Symbol target, Symbol... clause) {

        //replace empty clause with the 'Empty' pseudo-terminal
        if (clause.length == 0) {
            clause = new Symbol[]{Empty};
        }

        if (target.isTerminal()) {
            throw new IllegalArgumentException("The target symbol cannot be a terminal !");
        }

        final BaseRule rule = new BaseRule(rules.size(), null, target, clause);
        addRule(rule);
        return new RuleSpecifier() {

            @Override
            public RuleSpecifier withAction(Consumer<AstNode> consumer) {
                rule.setAction(consumer);
                return this;
            }

            @Override
            public RuleSpecifier withName(String name) {
                rule.setName(name);
                return this;
            }

            @Override
            public RuleSpecifier withPrecedenceLevel(int level) {
                return this;
            }

            @Override
            public Rule get() {
                return rule;
            }

            @Override
            public RuleSpecifier preferReduceOverShift() {
                rule.setConflictArbitration(ActionType.Reduce);
                return this;
            }

            @Override
            public RuleSpecifier preferShiftOverReduce() {
                rule.setConflictArbitration(ActionType.Shift);
                return this;
            }

            @Override
            public RuleSpecifier withAssociativity(Associativity associativity) {
                assert associativity != null;
                switch (associativity) {
                    case NonAssoc:
                        rule.setConflictArbitration(ActionType.Fail);
                        break;
                    case Right:
                        rule.setConflictArbitration(ActionType.Shift);
                        break;
                    case Left:
                        rule.setConflictArbitration(ActionType.Reduce);
                        break;
                }
                return this;
            }
        };
    }

    public Set<Lexeme> getTerminals() {
        Set<Lexeme> terminals = new HashSet<>();
        for (Rule r : rules) {
            for (Symbol s : r.getClause()) {
                if (s != Empty && s.isTerminal()) {
                    terminals.add((Lexeme) s);
                }
            }
        }
        return terminals;
    }

    public Set<Symbol> getSymbols() {
        Set<Symbol> symbols = new HashSet<>();
        for (Rule r : rules) {
            symbols.add(r.getTarget());
            for (Symbol s : r.getClause()) {
                if (s.isTerminal()) {
                    symbols.add(s);
                }
            }
        }
        return symbols;
    }

    public Set<Symbol> getNonTerminals() {
        return rules.stream().map(r -> r.getTarget()).collect(Collectors.toSet());
    }

    public String getName() {
        return name;
    }

    public void setTargetRule(Rule rule) {
        setTargetSymbol(rule.getTarget());
        if (rule.equals(getRuleById(0))) {
            return;
        }
        if (!rules.contains(rule)) {
            throw new IllegalArgumentException("Unknown rule :" + rule);
        }
        List<Rule> lRules = new ArrayList<>(rules);
        lRules.remove(rule);
        lRules.add(0, rule);
        int i = 0;
        for (Rule r : lRules) {
            r.setId(i++);
        }
        rules = new HashSet<>(lRules);
    }

    public void setName(String name) {
        this.name = name;
    }

    public Symbol getTargetSymbol() {
        return targetSymbol;
    }

    public void setTargetSymbol(Symbol targetSymbol) {
        this.targetSymbol = targetSymbol;
    }

    @Override
    public String toString() {

        List<Rule> lRules = new ArrayList(rules);
        Collections.sort(lRules, Comparator.comparing(Rule::getId));

        StringWriter sw = new StringWriter();
        if (name != null) {
            sw.append(name);
            sw.append(" : ");
        }
        sw.append("{\n");
        for (Rule r : lRules) {
            sw.append("    " + r.toString());
            sw.append("\n");
        }
        sw.append("}\n");
        return sw.toString();
    }

    public Rule getRuleById(int id) {
        Optional<Rule> opt = rules.stream().filter(r -> r.getId() == id).findAny();
        return opt.isPresent() ? opt.get() : null;
    }

    public Parser createParser() {
        return createParser(getTargetSymbol());
    }

    private void fixPrecedenceLevels() {
        Map<Rule, Integer> mRules = new HashMap<>();
        for (Rule rule : rules) {
            List<Symbol> clause = new ArrayList<>(Arrays.asList(rule.getClause()));
            Collections.reverse(clause);
            for (Symbol s : clause) {
                if (s.isTerminal() && precedenceLevels.containsKey(s)) {
                    int level = precedenceLevels.get(s);
                    mRules.put(rule, level);
                    rules.stream().filter(r -> Arrays.asList(r.getClause()).contains(rule.getTarget())).forEach(r -> {
                        mRules.put(r, level);
                    });
                    break;
                }
            }
        }
        for (Map.Entry<Rule, Integer> entry : mRules.entrySet()) {
            getLog().debug(String.format("Precedence Level %d : %s", entry.getValue(), entry.getKey()));
            ((BaseRule) entry.getKey()).setPrecedenceLevel(entry.getValue());
        }
    }

    public Parser createParser(Symbol symbol) {
        Grammar grammar = getSubGrammar(symbol);
        Rule targetRule = grammar.getRulesTargeting(grammar.getTargetSymbol()).iterator().next();
        ActionTable actionTable = ActionTable.lalr1(grammar, targetRule);
        return new LRParser(grammar, targetRule, actionTable);
    }

    protected Grammar getSubGrammar(Symbol symbol) {
        fixPrecedenceLevels();
        Set<Rule> targetRules = getRulesTargeting(symbol);
        if (targetRules.isEmpty()) {
            throw new IllegalArgumentException(String.format("Symbol '%s' is not a target for this grammar", symbol));
        } else if (targetRules.size() == 1) {
            Rule targetRule = targetRules.iterator().next();
            setTargetRule(targetRule);
            return this;
        } else {
            //ensure that we have a target rule that appear only once
            Symbol start = new Forward("(all)");
            Grammar cleanGrammar = new Grammar();
            cleanGrammar.precedenceLevels = precedenceLevels;
            if(name != null) {
                cleanGrammar.name = String.format("Subgrammar of '%s' targeting '%s'", name, symbol.toString());
            }
            //find all the rules that depend on the target rules
            HashSet<Symbol> seen = new HashSet<>();
            seen.add(symbol);

            Stack<Rule> stack = new Stack<>();
            stack.addAll(targetRules);

            //add all the rules with a definition that depend on the target symbol, recursively
            while (!stack.isEmpty()) {
                Rule rule = stack.pop();
                cleanGrammar.addRule(rule);
                seen.add(rule.getTarget());
                for (Symbol s : rule.getClause()) {
                    if (!s.isTerminal() && !seen.contains(s)) {
                        rules.stream().filter(r -> r.getTarget().equals(s)).forEach(r -> stack.push(r));
                    }
                }
            }

            Rule startRule = cleanGrammar.addRule(start, symbol).get();
            cleanGrammar.setTargetRule(startRule);
            return cleanGrammar;
        }
    }

    public ActionTable getActionTable(Symbol symbol) {
        Grammar grammar = getSubGrammar(symbol);
        Rule targetRule = grammar.getRulesTargeting(grammar.getTargetSymbol()).iterator().next();
        return ActionTable.lalr1(grammar, targetRule);
    }

    public Set<Rule> getRulesTargeting(Symbol symbol) {
        return rules.stream().filter(r -> r.getTarget().equals(symbol)).collect(Collectors.toSet());
    }

    /**
     * shift/reduce conflict resolution
     * <p>
     * <p>
     * The resolution of conflicts works by comparing the precedence of the rule being considered with that of the look-ahead token.
     * If the token's precedence is higher, the choice is to shift.
     * If the rule's precedence is higher, the choice is to reduce.
     * If they have equal precedence, the choice is made based on the associativity of that precedence level
     *
     * @param rule
     * @param symbol
     * @return
     */
    public ActionType getConflictResolutionHint(Rule rule, Symbol symbol) {
        ActionType decision = null;

        int rulePrecedence = ((BaseRule) rule).getPrecedenceLevel();
        int tokenPrecedence = getPrecedenceLevel(symbol);

        if (tokenPrecedence > rulePrecedence) {
            getLog().debug(String.format("{rule=%s, symbol=%s} : Shift ((tokenPrecedence=%d) > (rulePrecedence=%d))", rule, symbol, tokenPrecedence, rulePrecedence));
            decision = ActionType.Shift;
        } else if (rulePrecedence > tokenPrecedence) {
            getLog().debug(String.format("{rule=%s, symbol=%s} : Reduce ((tokenPrecedence=%d) < (rulePrecedence=%d))", rule, symbol, tokenPrecedence, rulePrecedence));
            decision = ActionType.Reduce;
        } else {
            decision = ((BaseRule) rule).getConflictArbitration();
            getLog().debug(String.format("{rule=%s, symbol=%s} : %s", rule, symbol, decision));
        }
        return decision;
    }

    private int getPrecedenceLevel(Symbol symbol) {
        Integer val = precedenceLevels.get(symbol);
        return val == null ? 0 : val;
    }

    public void setPrecedenceLevel(int level, Symbol... symbols) {
        //set precedence for the symbols
        for (Symbol symbol : symbols) {
            precedenceLevels.put(symbol, level);
        }
    }

    public Forward oneOf(Symbol... symbols) {
        assert symbols.length > 1;
        Forward tmp = new Forward("oneOf(" + String.join(", ", Arrays.asList(symbols).stream().map(Symbol::toString).collect(Collectors.toList())) + ")");
        for (Symbol s : symbols) {
            addRule(tmp, s);
        }
        return tmp;
    }

    public Forward optional(Symbol... symbols) {
        Forward opt = new Forward("optional(" + String.join(", ", Arrays.asList(symbols).stream().map(Symbol::toString).collect(Collectors.toList())) + ")");
        addRule(opt, symbols);
        addEmptyRule(opt);
        return opt;
    }

    public Forward oneOrMore(Symbol... symbols) {
        Forward tmp = new Forward();
        tmp.setName("oneOrMore" + tmp.hashCode());
        addRule(tmp, symbols);
        List<Symbol> list = new ArrayList<>();
        list.add(tmp);
        list.addAll(Arrays.asList(symbols));
        addRule(tmp, list);
        return tmp;
    }

    public Forward zeroOrMore(Symbol... symbols) {
        Forward tmp = new Forward();
        tmp.setName("zeroOrMore" + tmp.hashCode());
        addRule(tmp, symbols);
        List<Symbol> list = new ArrayList<>();
        list.add(tmp);
        list.addAll(Arrays.asList(symbols));
        addRule(tmp, list);
        addRule(tmp, Empty);
        return tmp;
    }

    public Forward list(Symbol separator, Symbol typeOfItems) {
        Forward tmp = new Forward("listOf(" + typeOfItems + ")");
        addRule(tmp, typeOfItems);
        addRule(tmp, tmp, separator, typeOfItems);
        addEmptyRule(tmp);
        return tmp;
    }
}

