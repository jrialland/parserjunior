package net.jr.util.zip;

import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;

public class ZipUtilTest {


    private Path makeDir() throws Exception {
        Path testFolder = Files.createTempDirectory("test");
        Path newFile = testFolder.resolve("example.txt");
        Files.write(newFile, "This is a test".getBytes());
        return testFolder;
    }

    @Test
    public void testZip() throws Exception {

        Path dir = makeDir();
        Path zipFile = Files.createTempFile("test", ".zip");

        Assert.assertTrue(zipFile.toString().endsWith(".zip"));

        ZipUtil.zip(dir, zipFile);

        Assert.assertTrue(Files.isRegularFile(zipFile));
        Assert.assertTrue(zipFile.toFile().length() > 0);

        Path outDir = Files.createTempDirectory("outdir");
        ZipUtil.unzip(zipFile, outDir);
        Assert.assertTrue(Files.isRegularFile(outDir.resolve(dir.getFileName() + "/example.txt")));
    }


}
