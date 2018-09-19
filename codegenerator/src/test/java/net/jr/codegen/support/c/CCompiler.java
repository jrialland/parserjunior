package net.jr.codegen.support.c;

import net.jr.io.IOUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;

/**
 * An utility class that invokes gcc in order to make
 * an executable from a simple C file
 */
public class CCompiler {

    public static String compileAndExecute(String code) throws IOException {
        ProcessBuilder pb = compile(code);
        Process p = pb.start();
        String result = new String(IOUtil.readFully(p.getInputStream()));
        try {
            if (p.waitFor() != 0) {
                throw new IllegalStateException("exit code : " + code);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public static ProcessBuilder compile(String code) throws IOException {
        return compile(new ByteArrayInputStream(code.getBytes()));
    }

    public static ProcessBuilder compile(InputStream cCode) throws IOException {

        Path tmpFile = Files.createTempFile("ccode", ".c");
        Files.copy(cCode, tmpFile, StandardCopyOption.REPLACE_EXISTING);

        Path exe = Paths.get(tmpFile.toString().replaceFirst("\\.c$", ""));

        ProcessBuilder pb = new ProcessBuilder("gcc", tmpFile.toString(), "-o", exe.toString());
        final int code;

        try {
            code = pb.start().waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Files.delete(tmpFile);

        if (code != 0) {
            throw new IllegalStateException("gcc exit code : " + code);
        }

        return new ProcessBuilder(exe.toString());
    }

}
