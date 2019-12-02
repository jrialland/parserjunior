package net.jr.io;

import java.io.IOException;
import java.io.OutputStream;

public class OutputStreamDelegate extends OutputStream {


    private OutputStream wrapped;

    private byte[] buf = new byte[]{0};

    public OutputStreamDelegate(OutputStream wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public final void write(int i) throws IOException {
        buf[0] = (byte) i;
        write(buf, 0, 1);
    }

    @Override
    public final void write(byte[] bytes) throws IOException {
        write(bytes, 0, bytes.length);
    }

    @Override
    public void write(byte[] bytes, int i, int i1) throws IOException {
        wrapped.write(bytes, i, i1);
    }

    @Override
    public void flush() throws IOException {
        wrapped.flush();
    }

    @Override
    public void close() throws IOException {
        wrapped.close();
    }

    public final OutputStream getWrapped() {
        return wrapped;
    }

}
