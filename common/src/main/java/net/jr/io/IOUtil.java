package net.jr.io;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

public final class IOUtil {

    private static final int DEFAULT_BUF_SIZE = 4096;
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

    public static OutputStream devNullOutputStream() {
        return NULL_OUTPUT_STREAM;
    }

    public static Writer devNull() {
        return NULL_WRITER;
    }

    public static Reader emptyReader() {
        return new StringReader("");
    }

    public static InputStream join(InputStream ... streams) {

        if(streams.length == 0) {
            return new ByteArrayInputStream(new byte[]{});
        }

        Iterator<InputStream> iterator = Arrays.asList(streams).iterator();

        return new InputStreamDelegate(iterator.next()) {

            private boolean atEnd = false;

            @Override
            public boolean markSupported() {
                return false;
            }


            @Override
            public int read(byte[] bytes, int offset, int len) throws IOException {

                if(atEnd) {
                    return -1;
                }

                int remaining = len;

                do {

                    int r = getWrapped().read(bytes, offset, remaining);

                    //stream closed
                    if (r == -1) {

                        //this was not the last one
                        if(iterator.hasNext()) {
                            //shift stream and continue reading
                            setWrapped(iterator.next());
                            offset += r;
                            remaining -= r;
                            continue;
                        } else {
                            atEnd = true;
                            return len - remaining;
                        }

                    }
                    remaining -=r;

                    if(remaining > 0) {
                        if (iterator.hasNext()) {
                            setWrapped(iterator.next());
                            offset += r;
                        }
                    }

                } while(remaining > 0);
                return len;
            }

            @Override
            public long skip(long l) throws IOException {
                long remaining = l;
                do {
                    long skipped = getWrapped().skip(remaining);
                    remaining -= skipped;
                    if(remaining > 0) {
                        if(iterator.hasNext()) {
                            setWrapped(iterator.next());
                        } else {
                            return l - remaining;
                        }
                    }
                } while(remaining > 0);
                return l;
            }

            @Override
            public void mark(int i) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void close() throws IOException {
                getWrapped().close();
                AtomicReference<IOException> excpt = new AtomicReference<>(null);
                try {
                    iterator.forEachRemaining( is -> {
                        try {
                            is.close();
                        } catch(IOException e) {
                            excpt.set(e);
                            throw new RuntimeException();
                        }
                    });
                } catch(Exception e) {
                    if(excpt.get() != null) {
                        throw excpt.get();
                    } else {
                        throw new IOException(e);
                    }
                }
            }
        };

    }
}
