package net.jr.jrc.qvm.memory;

import net.jr.io.IOUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;

public class PagedMemoryTest {

    @Test
    public void testMax() {
        PagedMemory pm = new PagedMemory(1024);
        Assert.assertEquals(0, pm.getUsedPages());
        pm.write(1022, "Hello".getBytes());
        Assert.assertEquals(2, pm.getUsedPages());
    }

    @Test
    public void testSmallPage() {
        PagedMemory pm = new PagedMemory(2);
        Assert.assertEquals(0, pm.getUsedPages());
        pm.write(1023, "This is a huge text !".getBytes());
        Assert.assertEquals(11, pm.getUsedPages());
        byte[] tmp = new byte[21];
        pm.read(1023, tmp);
        Assert.assertEquals("This is a huge text !", new String(tmp));
    }

    @Test
    public void testLimit() {
        PagedMemory pm = new PagedMemory();
        long max = pm.getMaxAddr();
        Assert.assertEquals(4294967296L, max);

        pm.write(678, new byte[]{(byte) -112});
        byte[] tmp = new byte[1];
        pm.read(678, tmp);
        Assert.assertEquals(-112, tmp[0]);

        pm.write(max - 1, new byte[]{(byte) 241});
        tmp = new byte[1];
        pm.read(max - 1, tmp);
        Assert.assertEquals((byte) 241, tmp[0]);

        try {
            pm.write(max + 1, new byte[]{(byte) 241});
            Assert.fail();
        } catch (MemoryException e) {
            // pass
        }

        try {
            pm.read(max + 1, new byte[]{(byte) 241});
            Assert.fail();
        } catch (MemoryException e) {
            // pass
        }

    }

    @Test
    public void testEmptyReader() throws Exception {
        PagedMemory mem = new PagedMemory();
        InputStream in = mem.createReader(mem.getMaxAddr() - 1);
        Assert.assertEquals(0, in.read());
        Assert.assertEquals(-1, in.read());
    }

    @Test
    public void testEmptyReader2() throws Exception {
        PagedMemory mem = new PagedMemory();
        InputStream in = mem.createReader(mem.getMaxAddr());
        Assert.assertEquals(0, IOUtil.readFully(in).length);
    }

    @Test(expected = MemoryException.class)
    public void testEmptyReader3() throws Exception {
        PagedMemory mem = new PagedMemory();
        InputStream in = mem.createReader(mem.getMaxAddr() + 1);
        in.close();
    }

}
