package net.jr.parser.impl;

import net.jr.common.Symbol;
import net.jr.lexer.Lexemes;
import net.jr.lexer.impl.SingleChar;
import net.jr.parser.Forward;
import net.jr.parser.Grammar;
import net.jr.parser.ast.AstNode;
import net.jr.parser.ast.annotations.Target;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

public class ActionTableTest {

    public static void setupClass() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
    }

    Symbol S = new Forward("S");
    Symbol N = new Forward("N");
    Symbol E = new Forward("E");
    Symbol V = new Forward("V");

    SingleChar x = new SingleChar('x');
    SingleChar eq = new SingleChar('=');
    SingleChar star = new SingleChar('*');

    Grammar grammar;

    @Before
    public void setup() {

        grammar = new Grammar();

        //1. S → N
        grammar.addRule(S, N);
        //2. N → V = E
        grammar.addRule(N, V, eq, E);
        //3. N → E
        grammar.addRule(N, E);
        //4. E → V
        grammar.addRule(E, V);
        //5. V → x
        grammar.addRule(V, x);
        //6. V → * E
        grammar.addRule(V, star, E);
    }

    private void assertContainsItem(Set<Item> set, String repr) {
        Assert.assertEquals(1, set.stream().filter(i -> i.toString().equals(repr)).count());
    }

    @Test
    public void testI0() {
        ActionTable.LALR1Builder builder = new ActionTable.LALR1Builder();
        ItemSet i0 = builder.getFirstItemSet(grammar);

        Assert.assertEquals(1, i0.getKernel().size());
        assertContainsItem(i0.getKernel(), "S → • N");

        Set<Item> closure = i0.getClosure();
        Assert.assertEquals(5, closure.size());

        assertContainsItem(closure, "N → • V '=' E");
        assertContainsItem(closure, "N → • E");
        assertContainsItem(closure, "E → • V");
        assertContainsItem(closure, "V → • 'x'");
        assertContainsItem(closure, "V → • '*' E");
    }

    @Test
    public void testAllItemSets() {
        ActionTable.LALR1Builder builder = new ActionTable.LALR1Builder();
        List<ItemSet> itemSets = new ArrayList<>(builder.getAllItemSets(grammar));
        itemSets.sort(Comparator.comparing(ItemSet::toString));
        for(ItemSet itemSet : itemSets) {
            System.out.println(itemSet);
        }
    }

    @Test
    public void testFirstSets() {
        ActionTable.LALR1Builder builder = new ActionTable.LALR1Builder();
        assertOnFirst(builder.getFirstSet(grammar, V));
        assertOnFirst(builder.getFirstSet(grammar, E));
        assertOnFirst(builder.getFirstSet(grammar, N));
        assertOnFirst(builder.getFirstSet(grammar, S));
        Set<ItemSet> itemSets = builder.getAllItemSets(grammar);
        Grammar extended = builder.makeExtendedGrammar(grammar.getRuleById(0), itemSets);
        for (Symbol nonTerminal : extended.getNonTerminals()) {
            assertOnFirst(builder.getFirstSet(extended, nonTerminal));
        }
    }

    private void assertOnFirst(Set<Symbol> f) {
        Assert.assertEquals(f.size(), 2);
        Assert.assertTrue(f.contains(x));
        Assert.assertTrue(f.contains(star));

    }

    @Test
    public void testExtendedGrammar() {
        ActionTable.LALR1Builder builder = new ActionTable.LALR1Builder();
        Set<ItemSet> itemSets = builder.getAllItemSets(grammar);
        Grammar extended = builder.makeExtendedGrammar(grammar.getRuleById(0), itemSets);
        ExtendedSymbol es = (ExtendedSymbol) extended.getTargetSymbol();
        Assert.assertEquals(S, es.getSymbol());

        Assert.assertEquals(6, grammar.getRules().size());
        Assert.assertEquals(12, extended.getRules().size());
    }

    @Test
    public void testExtendedGrammar2() {

        Grammar g = new Grammar();
        Symbol L = new Forward("list");
        Symbol I = new Forward("inList");
        Symbol ident = Lexemes.cIdentifier();

        g.addRule(L, new SingleChar('('), I, new SingleChar(')'));
        g.addRule(I, ident);
        g.addRule(I, I, new SingleChar(','), ident);
        g.addEmptyRule(I);

        List<ItemSet> itemSets = new ArrayList<>(new ActionTable.LALR1Builder().getAllItemSets(g));
        itemSets.sort(Comparator.comparing(ItemSet::toString));
        for(ItemSet itemSet : itemSets) {
            System.out.println(itemSet);
        }

        //ActionTable.LALR1Builder builder = new ActionTable.LALR1Builder();
        //Set<ItemSet> itemSets = builder.getAllItemSets(g);
        //Grammar extended = builder.makeExtendedGrammar(g.getRuleById(0), itemSets);
        //ExtendedSymbol es = (ExtendedSymbol) extended.getTargetSymbol();

    }
    @Test
    public void testFollowSets() {

        ActionTable.LALR1Builder builder = new ActionTable.LALR1Builder();
        Set<ItemSet> itemSets = builder.getAllItemSets(grammar);
        Grammar extended = builder.makeExtendedGrammar(grammar.getRuleById(0), itemSets);

        ExtendedSymbol eS = extended.getNonTerminals().stream().map(s -> (ExtendedSymbol) s).filter(s -> s.getSymbol().equals(S)).findAny().get();


        Map<Symbol, Set<? extends Symbol>> followSets = builder.getFollowSets(extended);
        for (Map.Entry<Symbol, Set<? extends Symbol>> entry : followSets.entrySet()) {
           //System.out.println(entry.getKey() + "    " + entry.getValue());
        }
    }

    @Test
    public void testCaching() {

        ActionTable actionTable = ActionTable.lalr1(grammar);

        ActionTableCaching.setEnabled(true);
        ActionTableCaching.get(grammar);
        ActionTable actionTable2 = ActionTableCaching.get(grammar);

        System.out.println(actionTable);
        System.out.println(actionTable2);

        for(Symbol terminal : grammar.getTerminals()) {
            for(int state =0; state < actionTable.getStatesCount(); state++) {

                Action a1 = actionTable.getAction(state, terminal);
                Action a2 = actionTable2.getAction(state, terminal);

                if(a1 == null) {
                    Assert.assertNull(a2);
                } else {
                    Assert.assertEquals(a1, a2);
                }
            }
        }

        for(Symbol nonTerminal : grammar.getNonTerminals()) {
            for(int state =0; state < actionTable.getStatesCount(); state++) {

                Action a1 = actionTable.getAction(state, nonTerminal);
                Action a2 = actionTable2.getAction(state, nonTerminal);

                if(a1 == null) {
                    Assert.assertNull(a2);
                } else {
                    Assert.assertEquals(a1, a2);
                }
            }
        }

        AstNode n1 = new LRParser(grammar, actionTable).parse("x=*x");
        System.out.println(n1);

        AstNode n2 = new LRParser(grammar, actionTable2).parse("x=*x");
        System.out.println(n2);
    }
}
