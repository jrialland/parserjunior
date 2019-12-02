package net.jr.cpreproc.procs;

import net.jr.common.Position;
import net.jr.cpreproc.macrodefs.DateMacroDefinition;
import net.jr.cpreproc.macrodefs.MacroDefinition;
import net.jr.cpreproc.macrodefs.NoArgsMacroDefinition;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class MacroExpanderTest {

    protected static Map<String, MacroDefinition> getDefinitions() {
        Map<String, MacroDefinition> defs = new HashMap<>();
        DateMacroDefinition.addDefinitions(defs);
        defs.put("FOO", new NoArgsMacroDefinition("FOO", "BAR"));
        return defs;
    }


    protected static void doTest(String expected, String tested) {
        String expanded = MacroExpander.expand(getDefinitions(), new PreprocessorLine(Position.unknown(), tested)).getText();
        Assert.assertEquals(expected, expanded);
    }

    @Test
    public void testNoChange() {
        String txt = "There should'nt be any change";
        doTest(txt, txt);
    }

    @Test
    public void testFoobar() {
        doTest("BAR", "FOO");
        doTest("BAR", "BAR");
    }

    @Test
    public void testExpandDate() {
        String expanded = MacroExpander.expand(getDefinitions(), new PreprocessorLine(Position.unknown(), "Today is __DATE__ ")).getText();
        System.out.println(expanded);
    }


}
