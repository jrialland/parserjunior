package net.jr.parser;

import org.junit.Assert;
import org.junit.Test;

import java.io.*;

public class NonTerminalTest {

    @Test
    public void testMarshall() throws IOException {
        NonTerminal f = new NonTerminal("test");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(baos);
        f.marshall(d);
        d.flush();

        NonTerminal f2 = NonTerminal.unMarshall(new DataInputStream(new ByteArrayInputStream(baos.toByteArray())));

        Assert.assertEquals(f.getName(), f2.getName());
        Assert.assertEquals(f, f2);

    }

    @Test
    public void testEquals() {
        NonTerminal f = new NonTerminal("test");
        Assert.assertFalse(f.equals(null));
        Assert.assertTrue(f.equals(f));

    }
}
