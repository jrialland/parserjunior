package net.jr.util.zip;

import net.jr.io.IOUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class ZipUtil {

    /**
     * unzip a file into a target folder.
     *
     * @param archiveFile
     * @param outputFolder
     * @throws IOException
     */
    public static void unzip(Path archiveFile, Path outputFolder) throws IOException {

        if (!Files.isRegularFile(archiveFile)) {
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

    public static void zip(Path folder, Path outputFile) throws IOException {

        if(!Files.isDirectory(folder)) {
            throw new IllegalArgumentException("not a folder : " + folder);
        }

        if(Files.exists(outputFile) && !Files.isRegularFile(outputFile)) {
            throw new IllegalArgumentException("not a regular file : " + outputFile);
        }

        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(outputFile.toFile()));

        String absFolder = folder.toAbsolutePath().toString().replace(File.separatorChar, '/');
        String[] parts = absFolder.split("/");
        String dirname = parts[parts.length-1];
        String base = absFolder.substring(0, absFolder.length() - dirname.length());

        Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                String filePath = path.toAbsolutePath().toString().substring(base.length());
                zipOutputStream.putNextEntry(new ZipEntry(filePath));
                IOUtil.copy(path.toUri().toURL().openStream(), zipOutputStream);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                String dirPath = path.toAbsolutePath().toString().substring(base.length()) + "/";
                zipOutputStream.putNextEntry(new ZipEntry(dirPath));
                return FileVisitResult.CONTINUE;
            }
        });
        zipOutputStream.flush();
        zipOutputStream.close();
    }
}
