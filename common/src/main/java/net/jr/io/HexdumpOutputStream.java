package net.jr.io;

import java.io.*;

public class HexdumpOutputStream extends OutputStream {


    private OutputStream wrapped;

    private long startAddr;

    private long addr;

    private StringBuilder ascDigest;

    private boolean asc;

    public HexdumpOutputStream(OutputStream wrapped) throws IOException {
        this(wrapped, 0);
    }

    public HexdumpOutputStream(OutputStream wrapped, long startAddr) throws IOException {
        this(wrapped, startAddr, true);
    }

    public HexdumpOutputStream(OutputStream wrapped, long startAddr, boolean asciiColumn) throws IOException {
        this.wrapped = wrapped;
        this.startAddr = startAddr;
        this.asc = asciiColumn;
        this.addr = startAddr - (startAddr % 16);
    }

    @Override
    public void write(int b) throws IOException {
        for (int i = 0, max = (int) (startAddr - addr); i < max; i++) {
            _write(-1, true);
        }
        _write(b, false);
    }

    private void _write(int b, boolean pad) throws IOException {
        StringBuilder sb = new StringBuilder();
        long r = addr % 16;
        if (r == 0) {
            sb.append(String.format("0x%08x", addr));
            sb.append(" ");
            if (asc) {
                ascDigest = new StringBuilder();
            }
        }
        if (asc) {
            if (pad) {
                ascDigest.append(".");
            } else {
                ascDigest.append(b > 31 && b < 127 ? (char) b : '.');
            }
        }
        sb.append(" ");
        if (pad) {
            sb.append("__");
        } else {
            sb.append(String.format("%02x", b & 0x00ff));
        }
        if (r == 7) {
            sb.append(" ");
        }
        if (r % 16 == 15) {
            if (asc) {
                sb.append("  |");
                sb.append(ascDigest.toString());
                sb.append("|");
                ascDigest = null;
            }
            sb.append("\n");
        }
        wrapped.write(sb.toString().getBytes());
        addr++;
    }

    @Override
    public void close() throws IOException {
        while (addr % 16 != 0) {
            _write(-1, true);
        }
        wrapped.flush();
        wrapped.close();
    }

    @Override
    public void flush() throws IOException {
        wrapped.flush();
    }

    public static String asHexDump(byte[] b) {
        try {
            return readFully(new ByteArrayInputStream(b));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String asHexDump(String s) {
        try {
            return readFully(new ByteArrayInputStream(s.getBytes()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String readFully(InputStream is) throws IOException {
        return readFully(is, 0);
    }

    public static String readFully(InputStream is, long offset) throws IOException {
        ByteArrayOutputStream tmp = new ByteArrayOutputStream();
        HexdumpOutputStream hd = new HexdumpOutputStream(tmp, offset);
        IOUtil.copy(is, hd);
        hd.close();
        return tmp.toString();
    }
}
