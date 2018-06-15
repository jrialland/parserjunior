package net.jr.cpreproc.macrodefs;

import net.jr.lexer.Token;

import java.util.List;

public interface MacroDefinition {

    String getName();

    boolean hasArgs();

    boolean isVariadic();

    List<String> getFormalParameters();

    List<Token> getReplacement();

    default String getReplacementAsString() {
        return getReplacement().stream().map(t -> t.getText()).reduce("", (a, b) -> a + b);
    }

}
