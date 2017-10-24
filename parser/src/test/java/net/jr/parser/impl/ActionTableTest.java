package net.jr.parser.impl;

import net.jr.common.Symbol;
import net.jr.lexer.impl.SingleChar;
import net.jr.parser.Forward;
import net.jr.parser.Grammar;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

public class ActionTableTest {

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

    @Test
    public void testFirstSets() {
        ActionTable.LALR1Builder builder = new ActionTable.LALR1Builder();
        assertOnFirst(builder.getFirst(grammar, V));
        assertOnFirst(builder.getFirst(grammar, E));
        assertOnFirst(builder.getFirst(grammar, N));
        assertOnFirst(builder.getFirst(grammar, S));
        Set<ItemSet> itemSets = builder.getAllItemSets(grammar, builder.getFirstItemSet(grammar, grammar.getRuleById(0)));
        Grammar extended = builder.makeExtendedGrammar(grammar, itemSets);
        for (Symbol nonTerminal : extended.getNonTerminals()) {
            assertOnFirst(builder.getFirst(extended, nonTerminal));
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
        Set<ItemSet> itemSets = builder.getAllItemSets(grammar, builder.getFirstItemSet(grammar, grammar.getRuleById(0)));
        Grammar extended = builder.makeExtendedGrammar(grammar, itemSets);
        ExtendedSymbol es = (ExtendedSymbol)extended.getTargetSymbol();
        Assert.assertEquals(S, es.getSymbol());
    }

    @Test
    public void testFollowSets() {

        ActionTable.LALR1Builder builder = new ActionTable.LALR1Builder();
        Set<ItemSet> itemSets = builder.getAllItemSets(grammar, builder.getFirstItemSet(grammar, grammar.getRuleById(0)));

        Grammar extended = builder.makeExtendedGrammar(grammar, itemSets);

        ExtendedSymbol eS = extended.getNonTerminals().stream().map(s->(ExtendedSymbol)s).filter(s->s.getSymbol().equals(S)).findAny().get();


        Map<Symbol, Set<Symbol>> followSets = builder.getFollowSets(extended, eS);
        for (Map.Entry<Symbol, Set<Symbol>> entry : followSets.entrySet()) {
            System.out.println(entry.getKey()+"    " + entry.getValue());
        }
    }
}
