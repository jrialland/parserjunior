package net.jr.io;

import java.io.*;
import java.nio.channels.FileChannel;

public final class IOUtil {

    private static final int DEFAULT_BUF_SIZE = 4096;

    public static InputStream readResource(String path) {
        return IOUtil.class.getClassLoader().getResourceAsStream(path);
    }

    public static InputStream readResource(Class<?> klass, String resName) {
        final String path = klass.getPackage().getName().replace('.', '/') + '/' + resName;
        return IOUtil.class.getClassLoader().getResourceAsStream(path);
    }

    public static String readFile(String filename) throws IOException {
        return readFully(new FileReader(filename));
    }

    public static String readFully(Reader in) {
        char[] buf = new char[DEFAULT_BUF_SIZE];
        int c;
        StringWriter sw = new StringWriter();
        try {
            while ((c = in.read(buf)) > -1) {
                sw.write(buf, 0, c);
            }
            in.close();
            return sw.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] readFully(InputStream is) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            IOUtil.copy(new BufferedInputStream(is), baos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }

    public static long copy(InputStream is, OutputStream os) throws IOException {
        if (is instanceof FileInputStream && os instanceof FileOutputStream) {
            FileChannel inChannel = ((FileInputStream) is).getChannel();
            FileChannel outChannel = ((FileOutputStream) os).getChannel();
            return inChannel.transferTo(0, inChannel.size(), outChannel);
        } else {
            byte[] buff = new byte[DEFAULT_BUF_SIZE];
            int c;
            long copied = 0;
            while ((c = is.read(buff)) > -1) {
                os.write(buff, 0, c);
                copied += c;
            }
            is.close();
            os.flush();
            return copied;
        }
    }

    private static final OutputStream NULL_OUTPUT_STREAM = new OutputStream() {
        @Override
        public void write(int i) throws IOException {

        }

        @Override
        public void write(byte[] bytes) throws IOException {
            super.write(bytes);
        }

        @Override
        public void write(byte[] bytes, int i, int i1) throws IOException {
        }
    };


    public static OutputStream devNullOutputStream() {
        return NULL_OUTPUT_STREAM;
    }

    private static final Writer NULL_WRITER = new Writer() {
        @Override
        public void write(char[] chars, int i, int i1) throws IOException {

        }

        @Override
        public void flush() throws IOException {

        }

        @Override
        public void close() throws IOException {

        }
    };

    public static Writer devNull() {
        return NULL_WRITER;
    }

    public static Reader emptyReader() {
        return new StringReader("");
    }
}
