package net.jr.cpreproc.macrodefs;

import net.jr.cpreproc.lexer.PreprocToken;
import net.jr.lexer.Token;

import java.util.List;

public interface MacroDefinition {

    String getName();

    boolean hasArgs();

    boolean isVariadic();

    List<String> getFormalParameters();

    List<PreprocToken> getReplacement(PreprocToken originalToken);
}
