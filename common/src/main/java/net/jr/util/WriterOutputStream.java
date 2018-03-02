package net.jr.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public class WriterOutputStream extends OutputStream {

    private Writer writer;

    private String charset;

    private byte[] buf = new byte[]{0};

    public WriterOutputStream(Writer writer, String charset) {
        this.writer = writer;
        this.charset = charset;
    }

    public WriterOutputStream(Writer writer) {
        this(writer, "utf-8");
    }

    @Override
    public void write(int i) throws IOException {
        buf[0] = (byte) i;
        write(buf, 0, 1);
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        writer.write(new String(b, off, len, charset));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !obj.getClass().equals(WriterOutputStream.class)) {
            return false;
        }
        final WriterOutputStream w = (WriterOutputStream) obj;
        return writer.equals(w.writer) && charset.equals(w.charset);
    }

    @Override
    public int hashCode() {
        return writer.hashCode() ^ charset.hashCode() >>> 3;
    }
}
