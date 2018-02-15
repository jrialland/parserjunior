package net.jr.lexer.expr;

import net.jr.common.Symbol;
import net.jr.lexer.Lexer;
import net.jr.lexer.LexerAlgorithm;
import org.junit.Ignore;

/**
 * Lexer test using the new 'merged' automaton algorithm
 */
@Ignore
public class MergedLexerTest extends AbstractLexerTestCases {

    @Override
    @SafeVarargs
    protected final <L extends Symbol> Lexer getLexer(L... tokenTypes) {
        Lexer lexer =  Lexer.forLexemes(tokenTypes);
        lexer.setLexerAlgorithm(LexerAlgorithm.Merged);
        return lexer;
    }
}
