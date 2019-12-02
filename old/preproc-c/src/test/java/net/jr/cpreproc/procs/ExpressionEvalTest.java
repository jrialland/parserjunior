package net.jr.cpreproc.procs;

import net.jr.cpreproc.macrodefs.MacroDefinition;
import net.jr.cpreproc.macrodefs.NoArgsMacroDefinition;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class ExpressionEvalTest {

    @Test
    public void testIsLegalConstant() {
        Assert.assertTrue(ExpressionEval.eval("3", Collections.EMPTY_MAP));
    }

    @Test
    public void testZero() {
        Assert.assertFalse(ExpressionEval.eval("0", Collections.EMPTY_MAP));
    }

    @Test
    public void testDefined() {
        Map<String, MacroDefinition> defs = new TreeMap<>();
        defs.put("FOO", new NoArgsMacroDefinition("FOO", "BAR"));
        Assert.assertTrue(ExpressionEval.eval("defined FOO", defs));
        Assert.assertTrue(ExpressionEval.eval("defined(FOO)", defs));
        Assert.assertFalse(ExpressionEval.eval("defined(BAR)", defs));
        Assert.assertFalse(ExpressionEval.eval("defined(BAZ)", defs));
    }

}
