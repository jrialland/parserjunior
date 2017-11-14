package net.jr.caching;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class DiskCache implements Cache<String, byte[]> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiskCache.class);

    private static final Logger getLog() {
        return LOGGER;
    }

    private Path tmpDir;

    private long ageLimit;

    public DiskCache(String name, int ttl, TimeUnit timeUnit) {
        ageLimit = timeUnit.toMillis(ttl);
        tmpDir = Paths.get(System.getProperty("java.io.tmpdir"), DiskCache.class.getName(), name);
        if (!Files.isDirectory(tmpDir)) {
            try {
                Files.createDirectories(tmpDir);
            } catch (IOException e) {
                getLog().error("Could not create temporary directory", e);
                tmpDir = null;
            }
        }
    }

    private static long getLastModified(Path path) throws IOException {
        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
        return attrs.lastModifiedTime().toInstant().getEpochSecond() * 1000;
    }

    private boolean isOutdated(Path path) throws IOException {
        long age = System.currentTimeMillis() - getLastModified(path);
        return age < ageLimit;
    }

    @Override
    public byte[] get(String key) {
        Path tmpFile = Paths.get(tmpDir.toString(), key);
        if (Files.isRegularFile(tmpFile)) {
            try {
                if (isOutdated(tmpFile)) {
                    Files.delete(tmpFile);
                    return null;
                } else {
                    byte[] data = Files.readAllBytes(tmpFile);
                    Files.setLastModifiedTime(tmpFile, FileTime.from(Instant.now()));
                    return data;
                }
            } catch (Exception e) {
                getLog().error("Could not get data", e);
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public void put(String key, byte[] data) {
        assert key != null;
        assert data != null;
        if (tmpDir != null) {
            Path tmpFile = Paths.get(tmpDir.toString(), key);
            try {
                Files.copy(new ByteArrayInputStream(data), tmpFile);
            } catch (Exception e) {
                getLog().error("Could not write item", e);
                if (Files.isRegularFile(tmpFile)) {
                    try {
                        Files.delete(tmpFile);
                    } catch (IOException deleteExcpt) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}
