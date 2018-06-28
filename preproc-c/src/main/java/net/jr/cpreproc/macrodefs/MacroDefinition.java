package net.jr.cpreproc.macrodefs;

import net.jr.lexer.Token;

import java.util.List;

public interface MacroDefinition {

    String getName();

    boolean hasArgs();

    boolean isVariadic();

    List<String> getFormalParameters();

    List<? extends Token> getReplacement(Token originalToken);
}
