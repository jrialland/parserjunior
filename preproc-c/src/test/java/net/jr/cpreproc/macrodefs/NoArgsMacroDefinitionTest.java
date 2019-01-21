package net.jr.cpreproc.macrodefs;

import net.jr.common.Position;
import net.jr.lexer.Token;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class NoArgsMacroDefinitionTest {

    @Test
    public void testSimple() {

        NoArgsMacroDefinition def = new NoArgsMacroDefinition("HELLO");

        Assert.assertTrue(def.getFormalParameters().isEmpty());
        Assert.assertEquals("HELLO", def.getName());
        Assert.assertEquals(null, def.getReplacement(null));

        def = new NoArgsMacroDefinition("HELLO", "Greetings");

        List<? extends Token> tokens = def.getReplacement(null);
        Assert.assertTrue(tokens.size() == 1);
        Assert.assertTrue(tokens.get(0).getPosition().equals(Position.start()));
        Assert.assertEquals("Greetings", tokens.get(0).getText());
        Assert.assertTrue(tokens.get(0).getTokenType().isTerminal());
        Assert.assertEquals("cIdentifier", tokens.get(0).getTokenType().toString());
    }

    @Test
    public void testMulti() {

        NoArgsMacroDefinition def = new NoArgsMacroDefinition("weird","}#|");
        List<? extends Token> tokens = def.getReplacement(null);

        Assert.assertTrue(tokens.size() == 3);
        Assert.assertTrue(tokens.get(0).getPosition().equals(Position.start()));
        Assert.assertTrue(tokens.get(1).getPosition().equals(Position.start().nextColumn()));
        Assert.assertTrue(tokens.get(2).getPosition().equals(Position.start().withOffset(2)));

        Assert.assertEquals("}", tokens.get(0).getText());
        Assert.assertEquals("#", tokens.get(1).getText());
        Assert.assertEquals("|", tokens.get(2).getText());

        Assert.assertEquals("NoMeaning", tokens.get(0).getTokenType().toString());
        Assert.assertEquals("StringifyOperator", tokens.get(1).getTokenType().toString());
        Assert.assertEquals("NoMeaning", tokens.get(2).getTokenType().toString());
    }
}
