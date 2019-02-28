package net.jr.jrc.qvm.asm;

import net.jr.jrc.qvm.QvmFile;
import net.jr.jrc.qvm.QvmInterpreter;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

public class AssemblerTest {

    @BeforeClass
    public static void setupClass() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
    }

    protected static Reader open(String rsc) {
        try {
            URL url = AssemblerTest.class.getClassLoader().getResource(rsc);
            Assert.assertNotNull(url);
            InputStream is = url.openStream();
            return new InputStreamReader(is);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testSection() throws Exception {
        Reader r = open("fibo.qvm");
        QvmFile qvmFile = new Assembler().assemble(r);
        Assert.assertEquals(1041, qvmFile.getBssLen());
        Assert.assertFalse(qvmFile.getInstructions().isEmpty());
    }

    /**
     * Assemble and run the fibonacci example from fibo.qvm, test if it gives correct results
     * @throws Exception
     */
    @Test
    public void testFibo() throws Exception {
        Assert.assertEquals(0, computeFibo(0));
        Assert.assertEquals(1, computeFibo(1));
        Assert.assertEquals(1, computeFibo(2)); // f(2) = f(1) + f(0) = 1 + 0
        Assert.assertEquals(2, computeFibo(3)); // f(3) = f(2) + f(1) = 1 + 1
        Assert.assertEquals(55, computeFibo(10));
    }

    protected long computeFibo(int n) throws Exception {
        Reader r = open("fibo.qvm");
        QvmFile qvmFile = new Assembler().assemble(r);
        QvmInterpreter interpreter = new QvmInterpreter();
        interpreter.getStack().pushUInt(n);
        return interpreter.run(qvmFile);
    }
}
