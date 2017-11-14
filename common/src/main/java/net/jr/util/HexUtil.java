package net.jr.util;

public final class HexUtil {

    final protected static char[] hexchars = "0123456789abcdef".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexchars[v >>> 4];
            hexChars[j * 2 + 1] = hexchars[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexToBytes(String hexString) {
        assert hexString.length() % 2 == 0;
        assert hexString.matches("^[0-9a-fA-F]*$");
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }

}
