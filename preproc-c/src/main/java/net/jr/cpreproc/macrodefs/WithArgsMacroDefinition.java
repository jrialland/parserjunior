package net.jr.cpreproc.macrodefs;

import net.jr.cpreproc.lexer.PreprocLexer;
import net.jr.lexer.Token;

import java.util.Collections;
import java.util.List;

public class WithArgsMacroDefinition implements MacroDefinition {

    private String name;

    private List<Token> replacement;

    private List<String> parameters;

    private boolean variadic;

    public WithArgsMacroDefinition(String name, String replacement, List<String> parameters, boolean variadic) {
        this.name = name;
        this.replacement = Collections.unmodifiableList(PreprocLexer.tokenize(replacement));
        this.parameters = parameters;
        this.variadic = variadic;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isVariadic() {
        return variadic;
    }

    @Override
    public boolean hasArgs() {
        return true;
    }

    @Override
    public List<Token> getReplacement(Token token) {
        return replacement;
    }

    @Override
    public List<String> getFormalParameters() {
        return parameters;
    }
}
