package net.jr.io;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamDelegate extends InputStream {

    private InputStream wrapped;

    private byte[] buf = new byte[]{0};

    public InputStreamDelegate(InputStream wrapped) {
        this.wrapped = wrapped;
    }

    public InputStream getWrapped() {
        return wrapped;
    }

    @Override
    public final int read() throws IOException {
        int r = this.read(buf, 0, 1);
        if (r == -1) {
            return -1;
        } else {
            return buf[0];
        }
    }

    @Override
    public final int read(byte[] bytes) throws IOException {
        return this.read(bytes, 0, bytes.length);
    }

    @Override
    public int read(byte[] bytes, int i, int i1) throws IOException {
        return wrapped.read(bytes, i, i1);
    }

    @Override
    public long skip(long l) throws IOException {
        return wrapped.skip(l);
    }

    @Override
    public int available() throws IOException {
        return wrapped.available();
    }

    @Override
    public void close() throws IOException {
        wrapped.close();
    }

    @Override
    public void mark(int i) {
        wrapped.mark(i);
    }

    @Override
    public void reset() throws IOException {
        wrapped.reset();
    }

    @Override
    public boolean markSupported() {
        return wrapped.markSupported();
    }
}
