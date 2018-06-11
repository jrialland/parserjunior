package net.jr.caching;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class DiskCache implements Cache<String, byte[]> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiskCache.class);
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

    private static final Logger getLog() {
        return LOGGER;
    }

    private static long getLastModified(Path path) throws IOException {
        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
        return attrs.lastModifiedTime().toInstant().getEpochSecond() * 1000;
    }

    private boolean isOutdated(Path path) throws IOException {
        long age = System.currentTimeMillis() - getLastModified(path);
        return age > ageLimit;
    }

    @Override
    public byte[] get(String key) {
        Path tmpFile = Paths.get(tmpDir.toString(), key);
        if (Files.isRegularFile(tmpFile)) {
            try {
                if (isOutdated(tmpFile)) {
                    Files.delete(tmpFile);
                    getLog().trace("DiskCache Miss : " + key);
                    return null;
                } else {
                    getLog().trace("DiskCache Hit : " + key);
                    byte[] data = Files.readAllBytes(tmpFile);
                    Files.setLastModifiedTime(tmpFile, FileTime.from(Instant.now()));
                    return data;
                }
            } catch (Exception e) {
                getLog().error("Could not get data", e);
                return null;
            }
        } else {
            getLog().trace("DiskCache Miss : " + key);
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
                getLog().trace("DiskCache Put : " + key);
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

    @Override
    public void evict(String key) {
        Path tmpFile = Paths.get(tmpDir.toString(), key);
        try {
            if (Files.isRegularFile(tmpFile)) {
                getLog().trace("DiskCache Evict : " + key);
                Files.delete(Paths.get(tmpDir.toString(), key));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void evictAll() {
        getLog().trace("DiskCache EvictAll");
        try {
            Files.walkFileTree(tmpDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
