package net.jr.parser;


import net.jr.common.Symbol;
import net.jr.lexer.Lexeme;
import net.jr.parser.impl.ActionTable;
import net.jr.parser.impl.BaseRule;
import net.jr.parser.impl.LRParser;

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

    public static final Symbol Empty = new Symbol() {
        @Override
        public boolean isTerminal() {
            return true;
        }

        @Override
        public String toString() {
            return "ε";
        }
    };

    private String name;

    private Set<Rule> rules = new HashSet<>();

    private Parser parser;

    public Grammar() {
        this(null);
    }

    public Grammar(String name) {
        this.name = name;
    }

    public Set<Rule> getRules() {
        return rules;
    }

    public interface RuleSpecifier {

        RuleSpecifier withAction(Consumer<Rule> consumer);

        RuleSpecifier withName(String name);

        Rule get();
    }

    public void addRule(Rule rule) {
        rules.add(rule);
    }

    public RuleSpecifier addRule(Symbol target, Symbol... clause) {

        //drop eventual parser
        parser =  null;

        //replace empty clause with the 'Empty' pseudo-terminal
        if(clause.length == 0) {
            clause = new Symbol[]{Empty};
        }

        if (target.isTerminal()) {
            throw new IllegalArgumentException("The target symbol cannot be a terminal !");
        }

        final BaseRule rule = new BaseRule(rules.size(), null, target, clause);
        addRule(rule);
        return new RuleSpecifier() {
            @Override
            public RuleSpecifier withAction(Consumer<Rule> consumer) {
                rule.setAction(consumer);
                return this;
            }

            @Override
            public RuleSpecifier withName(String name) {
                rule.setName(name);
                return this;
            }

            @Override
            public Rule get() {
                return rule;
            }
        };
    }

    public Set<Lexeme> getTerminals() {
        Set<Lexeme> terminals = new HashSet<>();
        for (Rule r : rules) {
            for (Symbol s : r.getClause()) {
                if (s.isTerminal()) {
                    terminals.add((Lexeme)s);
                }
            }
        }
        return terminals;
    }

    public Symbol or(Symbol ... syms) {
        assert syms.length>1;
        Symbol tmp = new Forward();
        for(Symbol s : syms) {
            addRule(tmp, s);
        }
        return tmp;
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
        if(rule.equals(getRuleById(0))) {
            return;
        }
        if(!rules.contains(rule)) {
            throw new IllegalArgumentException("Unknown rule :" + rule);
        }
        List<Rule> lRules = new ArrayList<>(rules);
        lRules.remove(rule);
        lRules.add(0, rule);
        int i=0;
        for(Rule r : lRules) {
            r.setId(i++);
        }
        rules = new HashSet<>(lRules);
    }


    /**
     * The rule that appears on left side but never on right side, if any
     * @return
     */
    public Symbol getTargetSymbol() {
       return getRuleById(0).getTarget();
    }

    @Override
    public String toString() {

        List<Rule> lRules = new ArrayList(rules);
        Collections.sort(lRules, Comparator.comparing(Rule::getId));

        StringWriter sw = new StringWriter();
        if(name != null) {
            sw.append(name);
            sw.append(" : ");
        }
        sw.append("{\n");
        for(Rule r : lRules) {
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
        if(parser ==  null) {
            parser = new LRParser(this, ActionTable.lalr1(this, getRuleById(0)));
        }
        return parser;
    }
}

