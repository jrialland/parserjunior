package net.jr.cpreproc.macrodefs;


import net.jr.cpreproc.lexer.PreprocLexer;
import net.jr.cpreproc.lexer.PreprocToken;

import java.util.Collections;
import java.util.List;

public class NoArgsMacroDefinition implements MacroDefinition {

    private String name;

    private List<PreprocToken> replacement;

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
    public List<PreprocToken> getReplacement(PreprocToken original) {
        return replacement;
    }
}
