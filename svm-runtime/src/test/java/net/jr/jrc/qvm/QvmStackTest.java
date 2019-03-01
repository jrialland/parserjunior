package net.jr.jrc.qvm;

import net.jr.io.HexdumpOutputStream;
import net.jr.jrc.qvm.memory.MemoryException;
import net.jr.jrc.qvm.memory.PagedMemory;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.Random;
import java.util.Stack;

public class QvmStackTest {

    @Test(expected = MemoryException.class)
    public void testPopEmptyStack() {
        QvmStack qvmStack = new QvmStack(new PagedMemory());
        qvmStack.popInt();
    }

    @Test
    public void testPushPop() {
        PagedMemory mem = new PagedMemory();
        QvmStack qvmStack = new QvmStack(mem);
        Random r = new Random();
        Stack<Integer> s = new Stack<>();
        for (int i = 0; i < 2000; i++) {
            int random = r.nextInt();
            s.push(random);
            qvmStack.pushInt(random);
        }
        for (int i = 0; i < 2000; i++) {
            Assert.assertEquals(s.pop().intValue(), qvmStack.popInt());
        }
        System.out.println(mem.getUsedPages() + "/" + mem.getMaxPages());
    }

    @Test
    public void testToString() throws Exception {
        PagedMemory mem = new PagedMemory();
        QvmStack qvmStack = new QvmStack(mem);
        qvmStack.pushFloat((float) Math.PI);
        long topOfStack = qvmStack.getTopOfStack();
        InputStream memReader = mem.createReader(topOfStack);
        System.out.println(HexdumpOutputStream.readFully(memReader, topOfStack));
    }

}
