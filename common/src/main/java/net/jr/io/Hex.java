package net.jr.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;

public final class Hex {

    public static final char[] hexChars = "0123456789abcdef".toCharArray();

    public static InputStream hexInputStream(InputStream wrapped) {
        return new InputStreamDelegate(wrapped) {

            @Override
            public int read(byte[] bytes, int off, int len) throws IOException {
                byte[] tmp = new byte[bytes.length * 2];
                int r = wrapped.read(tmp, 0, len * 2) /2;
                int rOff = 0;
                for(int i=0, max= Math.min(r, len); i<max; i++) {
                    byte h = tmp[rOff++];
                    byte l = tmp[rOff++];
                    bytes[off+i] = (byte) ((charToByte((char) h) << 4) + charToByte((char) l));
                }
                return r;
            }

            @Override
            public long skip(long l) throws IOException {
                return super.skip(l * 2);
            }

            @Override
            public void mark(int i) {
                super.mark(i * 2);
            }

            @Override
            public int available() throws IOException {
                return wrapped.available() / 2;
            }
        };
    }

    public static OutputStream hexOutputStream(OutputStream wrapped, boolean uppercase) {
        return new OutputStreamDelegate(wrapped) {

            @Override
            public void write(byte[] bytes, int off, int len) throws IOException {
                for (int i = 0; i < len; i++) {
                    final byte b = bytes[off + i];
                    wrapped.write(hexChars[b >>> 4]);
                    wrapped.write(hexChars[b & 0x0f]);
                }
            }
        };
    }

    private static byte charToByte(char c) {
        if (c >= '0' && c <= '9') {
            return (byte) (c - '0');
        } else if (c >= 'A' && c <= 'F') {
            return (byte) (10 + c - 'A');
        } else if (c >= 'a' && c <= 'f') {
            return (byte) (10 + c - 'a');
        } else throw new IllegalArgumentException("'" + c + "'");
    }

    public static byte[] fromHex(String hexString) {
        assert hexString != null;
        char[] data = hexString.toCharArray();
        assert data.length % 2 == 0;

        boolean even = true;
        byte v = 0;
        int offset = 0;
        byte[] array = new byte[data.length / 2];
        for (char c : data) {
            if (even) {
                v = (byte) (charToByte(c) << 4);
            } else {
                v += charToByte(c);
                array[offset++] = v;
            }
            even = !even;
        }
        return array;
    }

    public static String toHex(byte[] data) {
        StringWriter sw = new StringWriter();
        for (byte b : data) {
            sw.write(hexChars[b >> 4 & 0x0f]);
            sw.write(hexChars[b & 0x0f]);
        }
        return sw.toString();
    }


}
