package net.jr.codegen.support.c;

import org.junit.Assert;
import org.junit.Test;


public class CCompilerTest {

    @Test
    public void testHelloWorld() throws Exception {
        String hello = CCompiler.compileAndExecute("int main(void) { printf(\"Hello, world !\\n\"); return 0; } ");
        Assert.assertEquals("Hello, world !\n", hello);
    }
}
