package net.jr.parser;

import org.junit.Assert;
import org.junit.Test;

import java.io.*;

public class ForwardTest {

    @Test
    public void testMarshall() throws IOException {
        Forward f = new Forward("test");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(baos);
        f.marshall(d);
        d.flush();

        Forward f2 = Forward.unMarshall(new DataInputStream(new ByteArrayInputStream(baos.toByteArray())));

        Assert.assertEquals(f.getName(), f2.getName());
        Assert.assertEquals(f, f2);

    }

    @Test
    public void testEquals() {
        Forward f = new Forward("test");
        Assert.assertFalse(f.equals(null));
        Assert.assertTrue(f.equals(f));

    }
}
