package net.jr.parser;


import net.jr.common.Symbol;
import net.jr.lexer.Lexeme;

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

    private static final Symbol[] EmptySymbolArray = new Symbol[]{};

    private String name;

    private Set<Rule> rules = new HashSet<>();

    public Grammar() {
        this(null);
    }

    public Grammar(String name) {
        this.name = name;
    }

    public static class Rule {
        private Derivation derivation = Derivation.None;
        private Consumer<Rule> action;
        private String name;
        private Symbol target;
        private Symbol[] clause;

        public Symbol[] getClause() {
            return clause;
        }

        public Symbol getTarget() {
            return target;
        }

        public Derivation getDerivation() {
            return derivation;
        }

        public Rule(Symbol target, Collection<? extends Symbol> clause) {
            this(null, target, clause);
        }

        public Rule(String name, Symbol target, Symbol ... clause) {
            this.name = name;
            this.target = target;
            this.clause = clause;
        }

        public Rule(String name, Symbol target, Collection<? extends Symbol> clause) {
            this(name, target, clause.toArray(EmptySymbolArray));
        }

        @Override
        public String toString() {
            StringWriter sw = new StringWriter();
            sw.append(target.toString());
            sw.append(" → ");
            if (clause.length == 0) {
                sw.append("ε");
            } else {
                sw.append(String.join(" ", Arrays.asList(clause).stream().map(s -> s.toString()).collect(Collectors.toList())));
            }
            sw.append(".");
            return sw.toString();
        }
    }

    public Set<Rule> getRules() {
        return rules;
    }

    public interface RuleSpecifier {

        RuleSpecifier withAction(Consumer<Rule> consumer);

        RuleSpecifier withName(String name);

    }

    public RuleSpecifier addRule(Symbol target, Collection<? extends Symbol> clause) {
        return addRule(target, clause.toArray(EmptySymbolArray));
    }

    public RuleSpecifier addRule(Symbol target, Symbol... clause) {

        if (target.isTerminal()) {
            throw new IllegalArgumentException("target symbol cannot be a terminal !");
        }

        final Rule rule = new Rule(null, target, clause);
        rules.add(rule);
        return new RuleSpecifier() {
            @Override
            public RuleSpecifier withAction(Consumer<Rule> consumer) {
                rule.action = consumer;
                return this;
            }

            @Override
            public RuleSpecifier withName(String name) {
                rule.name = name;
                return this;
            }
        };
    }

    public Set<Lexeme> getTerminals() {
        HashSet<Lexeme> terminals = new HashSet<>();
        for (Rule r : rules) {
            for (Symbol s : r.getClause()) {
                if (s.isTerminal()) {
                    terminals.add((Lexeme) s);
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

    public List<Symbol> getSymbols() {
        List<Symbol> list = new ArrayList<>();
        list.addAll(getTerminals());
        list.addAll(getNonTerminals());
        return list;
    }

    public Set<Symbol> getNonTerminals() {
        return rules.stream().map(r -> r.getTarget()).collect(Collectors.toSet());
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        if(name != null) {
            sw.append(name);
            sw.append(" : ");
        }
        sw.append("{\n");
        for(Rule r : getRules()) {
            sw.append("    " + r.toString());
            sw.append("\n");
        }
        sw.append("}\n");
        return sw.toString();
    }
}

