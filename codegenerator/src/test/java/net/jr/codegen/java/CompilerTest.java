package net.jr.codegen.java;


import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;
import java.lang.reflect.Method;

public class CompilerTest {

    @Test
    public void doTest() throws Exception {
        String code = "public class Example { public String hello(){return \"Hello!\";}}";
        Class<?> clazz = Compiler.compile("Example", new StringReader(code));
        Assert.assertNotNull(clazz);

        Method m = clazz.getMethod("hello");
        String result = m.invoke(clazz.newInstance()).toString();
        Assert.assertEquals("Hello!", result);

    }


}
