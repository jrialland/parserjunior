package net.jr.util;

import net.jr.io.IOUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;

public class IOUtilTest {

    @Test
    public void testReadFile() throws IOException {
        Path tmpFile = Files.createTempFile("test", "txt");
        Files.write(tmpFile, "Hello".getBytes());
        String r = IOUtil.readFile(tmpFile.toString());
        Assert.assertEquals("Hello", r);
        Files.delete(tmpFile);
    }

    @Test
    public void testReadFully() {
        String test = "This is a test";
        String result = IOUtil.readFully(new StringReader(test));
        Assert.assertEquals(test, result);
        byte[] bytes = IOUtil.readFully(new ByteArrayInputStream(test.getBytes()));
        Assert.assertTrue(Arrays.equals(test.getBytes(), bytes));
    }

    @Test
    public void testReadFully2() throws IOException {
        byte[] data = new byte[8192];
        new Random().nextBytes(data);
        byte[] readData = IOUtil.readFully(new ByteArrayInputStream(data));
        Assert.assertTrue(Arrays.equals(data, readData));
    }

    @Test
    public void testCopy() throws IOException {
        byte[] data = new byte[8192];
        new Random().nextBytes(data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtil.copy(new ByteArrayInputStream(data), baos);
        Assert.assertTrue(Arrays.equals(data, baos.toByteArray()));
    }

    @Test
    public void testCopyWithFiles() throws IOException {
        byte[] data = new byte[8192];
        new Random().nextBytes(data);
        File inFile = File.createTempFile("test", ".in");
        File outFile = File.createTempFile("test", ".out");
        inFile.deleteOnExit();
        outFile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(inFile);
        fos.write(data);
        fos.flush();
        fos.close();
        IOUtil.copy(new FileInputStream(inFile), new FileOutputStream(outFile));
        byte[] readData = IOUtil.readFully(new FileInputStream(outFile));
        Assert.assertTrue(Arrays.equals(data, readData));
        inFile.delete();
        outFile.delete();
    }

    @Test
    public void testReadResource() {
        String data = new String(IOUtil.readFully(IOUtil.readResource("test.txt")));
        Assert.assertEquals("Hello !", data);
    }

    @Test
    public void testReadResource2() {
        String data = new String(IOUtil.readFully(IOUtil.readResource(IOUtil.class, "utilresource.txt")));
        Assert.assertEquals("Resource file in a package", data);
    }
}
