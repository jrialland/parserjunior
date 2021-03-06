package net.jr.parser.impl;

import net.jr.common.Symbol;
import net.jr.parser.ParsingContext;
import net.jr.parser.Rule;

import java.util.function.Consumer;

/**
 * Base implementation of a grammar {@link Rule}.
 */
public class BaseRule extends Rule {

    private Consumer<ParsingContext> action;

    private String name;

    private String comment;

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

    public Consumer<ParsingContext> getAction() {
        return action;
    }

    public void setAction(Consumer<ParsingContext> action) {
        this.action = action;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Symbol[] getClause() {
        return clause;
    }

    public Symbol getTarget() {
        return target;
    }

    public Integer getPrecedenceLevel() {
        return precedenceLevel;
    }

    public void setPrecedenceLevel(int precedenceLevel) {
        this.precedenceLevel = precedenceLevel;
    }

    public ActionType getConflictArbitration() {
        return conflictArbitration;
    }

    public void setConflictArbitration(ActionType conflictArbitration) {
        this.conflictArbitration = conflictArbitration;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
