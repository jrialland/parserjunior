package net.jr.parser.impl;

import net.jr.common.Symbol;
import net.jr.parser.Derivation;
import net.jr.parser.ParsingContext;
import net.jr.parser.Rule;
import net.jr.parser.ast.AstNode;

import java.util.function.Consumer;

/**
 * Base implementation of a grammar {@link Rule}.
 */
public class BaseRule extends Rule {

    private Derivation derivation = Derivation.None;

    private Consumer<ParsingContext> action;

    private String name;

    private Symbol target;

    private Symbol[] clause;

    private ActionType conflictArbitration;

    private int precedenceLevel;

    public BaseRule(int id, String name, Symbol target, Symbol... clause) {
        setId(id);
        this.name = name;
        this.target = target;
        this.clause = clause;
    }

    public void setAction(Consumer<ParsingContext> action) {
        this.action = action;
    }

    public Consumer<ParsingContext> getAction() {
        return action;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Symbol[] getClause() {
        return clause;
    }

    public Symbol getTarget() {
        return target;
    }

    public Derivation getDerivation() {
        return derivation;
    }

    public void setPrecedenceLevel(int precedenceLevel) {
        this.precedenceLevel = precedenceLevel;
    }

    public Integer getPrecedenceLevel() {
        return precedenceLevel;
    }

    public void setConflictArbitration(ActionType conflictArbitration) {
        this.conflictArbitration = conflictArbitration;
    }

    public ActionType getConflictArbitration() {
        return conflictArbitration;
    }
}
