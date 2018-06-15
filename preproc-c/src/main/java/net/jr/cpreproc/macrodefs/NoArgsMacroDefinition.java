package net.jr.cpreproc.macrodefs;


import net.jr.lexer.Token;

import java.util.Collections;
import java.util.List;

public class NoArgsMacroDefinition implements MacroDefinition {

    private String name;

    private List<Token> replacement;

    protected NoArgsMacroDefinition(String name) {
        this.name = name;
    }

    public NoArgsMacroDefinition(String name, String replacement) {
        this(name);
        this.replacement = null;//Collections.unmodifiableList(Tokenizer.tokenize(replacement));
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
    public List<Token> getReplacement() {
        return replacement;
    }
}
