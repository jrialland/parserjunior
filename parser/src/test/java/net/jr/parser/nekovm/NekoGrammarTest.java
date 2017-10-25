package net.jr.parser.nekovm;

import net.jr.parser.impl.ActionTable;
import org.junit.Test;

public class NekoGrammarTest {

    @Test
    public void testCompileParser() {
        NekoGrammar ng = new NekoGrammar();
        ActionTable actionTable = ActionTable.lalr1(ng, ng.getRuleById(0));
        System.out.println(actionTable);
    }

}
