package net.jr.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public class HashUtil {

    private static final String ALG_MD5 = "MD5";

    private static final String ALG_SHA1 = "SHA-1";

    private static final String ALG_SHA256 = "SHA-256";


    public static String md5Hex(byte[] data) {
        return HexUtil.bytesToHex(md5(data));
    }

    public static String sha1Hex(byte[] data) {
        return HexUtil.bytesToHex(sha1(data));
    }

    public static String sha256Hex(byte[] data) {
        return HexUtil.bytesToHex(sha256(data));
    }

    public static byte[] md5(InputStream data) {
        return hash(ALG_MD5, data);
    }

    public static byte[] sha1(InputStream data) {
        return hash(ALG_SHA1, data);
    }

    public static byte[] sha256(InputStream data) {
        return hash(ALG_SHA256, data);
    }

    public static byte[] sha256Twice(InputStream data) {
        return sha256(sha256(data));
    }

    public static byte[] md5(byte[] data) {
        return md5(new ByteArrayInputStream(data));
    }

    public static byte[] sha1(byte[] data) {
        return sha1(new ByteArrayInputStream(data));
    }

    public static byte[] sha256(byte[] data) {
        return sha256(new ByteArrayInputStream(data));
    }

    public static byte[] sha256Twice(byte[] data) {
        return sha256Twice(new ByteArrayInputStream(data));
    }

    private static byte[] hash(String alg, InputStream data) {
        try {
            MessageDigest md = MessageDigest.getInstance(alg);
            byte[] buff = new byte[1024];
            int c = 0;
            while ((c = data.read(buff)) > -1) {
                md.update(buff, 0, c);
            }
            return md.digest();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
