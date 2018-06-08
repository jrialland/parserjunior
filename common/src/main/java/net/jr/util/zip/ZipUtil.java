package net.jr.util.zip;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class ZipUtil {

    /**
     * unzip a file into a target folder.
     *
     * @param archiveFile
     * @param outputFolder
     * @throws IOException
     */
    public static void unzip(Path archiveFile, Path outputFolder) throws IOException {

        if(!Files.isRegularFile(archiveFile)) {
            throw new IllegalArgumentException("not a regular file : " + archiveFile);
        }

        if (!Files.isDirectory(outputFolder)) {
            Files.createDirectories(outputFolder);
        }

        ZipInputStream zIs = new ZipInputStream(archiveFile.toUri().toURL().openStream());

        ZipEntry ze;
        while ((ze = zIs.getNextEntry()) != null) {
            String fPath = ze.getName();
            Path newFile = outputFolder.resolve(fPath);
            if (fPath.endsWith("/")) {
                if (!Files.isDirectory(newFile)) {
                    Files.createDirectories(newFile);
                }
            } else {
                if (!Files.isDirectory(newFile.getParent())) {
                    Files.createDirectories(newFile.getParent());
                }
                Files.copy(zIs, newFile, StandardCopyOption.REPLACE_EXISTING);
            }
        }

    }
}
