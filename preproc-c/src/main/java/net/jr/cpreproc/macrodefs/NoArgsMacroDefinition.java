package net.jr.cpreproc.macrodefs;


import net.jr.cpreproc.lexer.PreprocLexer;
import net.jr.lexer.Token;

import java.util.Collections;
import java.util.List;

public class NoArgsMacroDefinition implements MacroDefinition {

    private String name;

    private List<? extends Token> replacement;

    protected NoArgsMacroDefinition(String name) {
        this.name = name;
    }

    public NoArgsMacroDefinition(String name, String replacement) {
        this(name);
        this.replacement = PreprocLexer.tokenize(replacement);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean hasArgs() {
        return false;
    }

    @Override
    public boolean isVariadic() {
        return false;
    }

    @Override
    public List<String> getFormalParameters() {
        return Collections.emptyList();
    }

    @Override
    public List<? extends Token> getReplacement(Token original) {
        return replacement;
    }
}
