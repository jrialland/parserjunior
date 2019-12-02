package net.jr.text;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

public class IndentWriterTest {

    @Test
    public void basicTest() throws IOException {

        StringWriter sw = new StringWriter();
        IndentWriter iw = new IndentWriter(sw, "\t");

        iw.write("Hello\n");
        iw.indent();
        iw.write("world\n");
        iw.deindent();
        iw.write("end of transmission");
        iw.flush();
        iw.close();
        Assert.assertEquals("Hello\n\tworld\nend of transmission", sw.toString());
    }

    @Test
    public void testStartIndented() throws IOException {

        StringWriter sw = new StringWriter();
        IndentWriter iw = new IndentWriter(sw, " ");

        iw.setIndentationLevel(4);
        iw.write("This\nis\na\ntes");

        Assert.assertEquals(4, iw.getIndentationLevel());
        Assert.assertEquals("    ", iw.getIndentation());
        iw.deindent();
        Assert.assertEquals("   ", iw.getIndentation());
        iw.setIndentationLevel(0);
        iw.write("t");
        Assert.assertEquals("    This\n    is\n    a\n    test", sw.toString());
    }


}
