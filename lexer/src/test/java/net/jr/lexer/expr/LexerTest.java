package net.jr.lexer.expr;

import net.jr.common.Symbol;
import net.jr.lexer.Lexer;
import net.jr.lexer.LexerAlgorithm;

/**
 * Lexer tests using the basic
 */
public class LexerTest extends AbstractLexerTestCases {

    @Override
    @SafeVarargs
    protected final <L extends Symbol> Lexer getLexer(L... tokenTypes) {
        Lexer lexer =  Lexer.forLexemes(tokenTypes);
        lexer.setLexerAlgorithm(LexerAlgorithm.Basic);
        return lexer;
    }
}
