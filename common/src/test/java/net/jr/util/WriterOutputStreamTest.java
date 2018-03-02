package net.jr.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

public class WriterOutputStreamTest {

    @Test
    public void testWriterOutputStream() throws IOException {
        StringWriter sw = new StringWriter();
        WriterOutputStream wos = new WriterOutputStream(sw);

        wos.write((int)'H');
        wos.write("ello ".getBytes());
        wos.write("XXXWorld".getBytes(), 3, 5);

        wos.flush();

        Assert.assertEquals("Hello World", sw.toString());

        WriterOutputStream wos2 = new WriterOutputStream(sw, "utf-8");
        Assert.assertTrue(wos != wos2);
        Assert.assertEquals(wos, wos2);
        Assert.assertEquals(wos.hashCode(), wos2.hashCode());
        Assert.assertFalse(wos.equals(null));
        Assert.assertFalse(wos.equals("invalid"));
    }

}
