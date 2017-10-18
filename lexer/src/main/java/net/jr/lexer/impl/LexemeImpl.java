package net.jr.lexer.impl;

import net.jr.lexer.Lexeme;

public abstract class LexemeImpl implements Lexeme {

    public abstract Automaton getAutomaton();

}
