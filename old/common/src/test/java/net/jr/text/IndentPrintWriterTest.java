package net.jr.text;

import org.junit.Assert;
import org.junit.Test;

import java.io.StringWriter;

public class IndentPrintWriterTest {

    @Test
    public void basicTest() {

        StringWriter sw = new StringWriter();
        IndentPrintWriter piw = new IndentPrintWriter(sw, "\t");

        piw.println("Hello");
        piw.indent();
        piw.println("world");
        piw.deindent();
        piw.write("end of transmission");
        piw.flush();
        Assert.assertEquals("Hello\n\tworld\nend of transmission", sw.toString());
    }

    @Test
    public void testStartIndented() {

        StringWriter sw = new StringWriter();
        IndentPrintWriter piw = new IndentPrintWriter(sw, " ");

        piw.setIndentationLevel(4);
        piw.println("This");
        piw.println("is");
        piw.println("a");
        piw.print("tes");

        Assert.assertEquals(4, piw.getIndentationLevel());
        Assert.assertEquals("    ", piw.getIndentation());
        piw.deindent();
        Assert.assertEquals("   ", piw.getIndentation());
        piw.setIndentationLevel(0);
        piw.write("t");
        piw.flush();

        Assert.assertEquals("    This\n    is\n    a\n    test", sw.toString());
    }
}
