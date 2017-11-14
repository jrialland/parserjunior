package net.jr.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public class HashUtil {

    public static String md5(byte[] data) {
        return hash("md5", new ByteArrayInputStream(data));
    }

    public static String sha1(byte[] data) {
        return hash("sha1", new ByteArrayInputStream(data));
    }

    private static String hash(String alg, InputStream data) {
        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            byte[] buff = new byte[1024];
            int c = 0;
            while ((c = data.read(buff)) > -1) {
                md.update(buff, 0, c);
            }
            return HexUtil.bytesToHex(md.digest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
