package net.jr.cpreproc.macrodefs;

import net.jr.common.Position;
import net.jr.cpreproc.lexer.PreprocLexer;
import net.jr.cpreproc.lexer.PreprocToken;
import net.jr.lexer.Token;
import org.junit.Assert;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class DateMacroDefinitionTest {

    private static final Date TEST_DATE;

    static {
        try {
            TEST_DATE = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2018-09-30 10:37:42");
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testTimestamp() throws Exception {
        doTest(DateMacroDefinition.TimeStampDefinition, "Sun Sep 30 10:37:42 2018");
    }

    @Test
    public void testTime() throws Exception {
        doTest(DateMacroDefinition.TimeDefinition, "10:37:42");
    }

    @Test
    public void testDate() throws Exception {
        doTest(DateMacroDefinition.DateDefinition, "Sep 30 2018");
    }

    void doTest(DateMacroDefinition dateMacroDefinition, String expectedString) throws Exception {

        DateMacroDefinition mocked = spy(dateMacroDefinition);
        when(mocked.getDate()).thenReturn(TEST_DATE);

        List<PreprocToken> tokens = PreprocLexer.tokenize("__DATE__");
        List<Token> replacement = mocked.getReplacement(tokens.get(0));

        Assert.assertTrue(replacement.size() == 1);
        Assert.assertEquals(expectedString, replacement.get(0).getText());
        Assert.assertEquals(Position.start(), replacement.get(0).getPosition());

    }
}
