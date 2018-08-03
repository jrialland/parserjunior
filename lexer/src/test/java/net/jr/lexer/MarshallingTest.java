package net.jr.lexer;

import net.jr.lexer.basicterminals.CString;
import net.jr.lexer.basicterminals.QuotedString;
import net.jr.marshalling.MarshallingUtil;
import net.jr.types.ProxyUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class MarshallingTest {

    @Test
    public void testCString() throws IOException {

        Terminal a = new CString().withName("a");
        Terminal b = new CString().withName("b");
        Terminal c = new CString().withName("c");

        List<Terminal> l = Arrays.asList(a, b, c);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        DataOutput out = ProxyUtil.snitchingProxy(DataOutput.class, new DataOutputStream(baos));


        MarshallingUtil.marshall(l, out);

        DataInput in = ProxyUtil.snitchingProxy(DataInput.class, new DataInputStream(new ByteArrayInputStream(baos.toByteArray())));

        Object obj = MarshallingUtil.unMarshall(in);

        List<CString> unmarshalled = (List<CString>) obj;

        Assert.assertEquals("a", unmarshalled.get(0).getName());
        Assert.assertEquals("b", unmarshalled.get(1).getName());
        Assert.assertEquals("c", unmarshalled.get(2).getName());


    }

    @Test
    public void testQuotedString() throws IOException {

        Terminal q = new QuotedString('"','"', '\\', new char[]{});

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        DataOutput out = ProxyUtil.snitchingProxy(DataOutput.class, new DataOutputStream(baos));
        MarshallingUtil.marshall(q, out);

        DataInput in = ProxyUtil.snitchingProxy(DataInput.class, new DataInputStream(new ByteArrayInputStream(baos.toByteArray())));
        Object obj = MarshallingUtil.unMarshall(in);

    }

}
