package net.jr.parser.impl;

/**
 * Type of decision that a parser may take. Used by {@link Action}
 */
public enum ActionType {
    Accept,
    Fail,
    Goto,
    Shift,
    Reduce
}
