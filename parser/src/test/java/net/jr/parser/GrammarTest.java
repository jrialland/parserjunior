package net.jr.parser;

import net.jr.common.Symbol;
import net.jr.lexer.CommonTokenTypes;
import net.jr.lexer.impl.SingleChar;
import net.jr.parser.impl.ActionTable;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Analysing the following set of rules in order to test that we produce the right action table
 * for an lalr parser.
 * <p>
 * <pre>
 * (1) E → E * B
 * (2) E → E + B
 * (3) E → B
 * (4) B → 0
 * (5) B → 1
 * </pre>
 * <p>
 * <p>
 * We verify that we generate the following table :
 * <p>
 * <tbody><tr align="center">
 * <td><i>état</i></td>
 * <td colspan="5"><i>action</i></td>
 * <td></td>
 * <td colspan="2"><i>branchement</i></td>
 * </tr>
 * <tr align="center">
 * <td></td>
 * <td><b>*</b></td>
 * <td><b>+</b></td>
 * <td><b>0</b></td>
 * <td><b>1</b></td>
 * <td><b>$</b></td>
 * <td>&nbsp;</td>
 * <td><b>E</b></td>
 * <td><b>B</b></td>
 * </tr>
 * <tr align="center">
 * <td><b>0</b></td>
 * <td>&nbsp;</td>
 * <td>&nbsp;</td>
 * <td>s1</td>
 * <td>s2</td>
 * <td>&nbsp;</td>
 * <td>&nbsp;</td>
 * <td>3</td>
 * <td>4</td>
 * </tr>
 * <tr align="center">
 * <td><b>1</b></td>
 * <td>r4</td>
 * <td>r4</td>
 * <td>r4</td>
 * <td>r4</td>
 * <td>r4</td>
 * <td></td>
 * <td>&nbsp;</td>
 * <td>&nbsp;</td>
 * </tr>
 * <tr align="center">
 * <td><b>2</b></td>
 * <td>r5</td>
 * <td>r5</td>
 * <td>r5</td>
 * <td>r5</td>
 * <td>r5</td>
 * <td></td>
 * <td>&nbsp;</td>
 * <td>&nbsp;</td>
 * </tr>
 * <tr align="center">
 * <td><b>3</b></td>
 * <td>s5</td>
 * <td>s6</td>
 * <td>&nbsp;</td>
 * <td></td>
 * <td>acc</td>
 * <td>&nbsp;</td>
 * <td>&nbsp;</td>
 * <td>&nbsp;</td>
 * </tr>
 * <tr align="center">
 * <td><b>4</b></td>
 * <td>r3</td>
 * <td>r3</td>
 * <td>r3</td>
 * <td>r3</td>
 * <td>r3</td>
 * <td>&nbsp;</td>
 * <td>&nbsp;</td>
 * <td>&nbsp;</td>
 * </tr>
 * <tr align="center">
 * <td><b>5</b></td>
 * <td>&nbsp;</td>
 * <td>&nbsp;</td>
 * <td>s1</td>
 * <td>s2</td>
 * <td></td>
 * <td>&nbsp;</td>
 * <td>&nbsp;</td>
 * <td>7</td>
 * </tr>
 * <tr align="center">
 * <td><b>6</b></td>
 * <td>&nbsp;</td>
 * <td>&nbsp;</td>
 * <td>s1</td>
 * <td>s2</td>
 * <td></td>
 * <td>&nbsp;</td>
 * <td>&nbsp;</td>
 * <td>8</td>
 * </tr>
 * <tr align="center">
 * <td><b>7</b></td>
 * <td>r1</td>
 * <td>r1</td>
 * <td>r1</td>
 * <td>r1</td>
 * <td>r1</td>
 * <td>&nbsp;</td>
 * <td>&nbsp;</td>
 * <td>&nbsp;</td>
 * </tr>
 * <tr align="center">
 * <td><b>8</b></td>
 * <td>r2</td>
 * <td>r2</td>
 * <td>r2</td>
 * <td>r2</td>
 * <td>r2</td>
 * <td>&nbsp;</td>
 * <td>&nbsp;</td>
 * <td>&nbsp;</td>
 * </tr>
 * </tbody>
 */
public class GrammarTest {

    @BeforeClass
    public static void setupClass() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
    }

    Symbol E = new Forward("E");

    /**
     * <pre>
     * (1) E → E * B
     * (2) E → E + B
     * (3) E → B
     * (4) B → 0
     * (5) B → 1
     * </pre>
     *
     * @return
     */
    private Grammar makeGrammar() {

        Grammar g = new Grammar();


        Symbol B = new Forward("B");

        // (1) E → E * B
        g.addRule(E, E, new SingleChar('*'), B).withName("(1)");

        // (2) E → E + B
        g.addRule(E, E, new SingleChar('+'), B).withName("(2)");

        // (3) E → B
        g.addRule(E, B).withName("(3)");

        //(4) B → 0
        g.addRule(B, new SingleChar('0')).withName("(4)");

        //(5) B → 1
        g.addRule(B, new SingleChar('1')).withName("(5)");

        return g;
    }

    @Test
    public void testGrammarHasTerminals() {
        Assert.assertEquals(4, makeGrammar().getTerminals().size());
    }

    @Test
    public void test2() {
        Grammar g = makeGrammar();
        Symbol S = new Forward("S");
        Rule rule = g.addRule(S, E, CommonTokenTypes.eof()).get();
        System.out.println(
        g.getNonTerminals());


        ActionTable actionTable = ActionTable.lalr1(g,rule);
        System.out.println(actionTable);
    }

    @Test
    public void test1() {

        Grammar g = new Grammar();

        Symbol S = new Forward("S");
        Symbol N = new Forward("N");
        Symbol E = new Forward("E");
        Symbol V = new Forward("V");

        //1. S → N
        g.addRule(S, N);
        //2. N → V = E
        g.addRule(N, V, new SingleChar('='), E);
        //3. N → E
        g.addRule(N, E);
        //4. E → V
        g.addRule(E, V);
        //5. V → x
        g.addRule(V, new SingleChar('x'));
        //6. V → * E
        g.addRule(V, new SingleChar('*'), E);

        ActionTable actionTable = ActionTable.lalr1(g, g.getRuleById(0));

        System.out.println(actionTable);

    }
}
