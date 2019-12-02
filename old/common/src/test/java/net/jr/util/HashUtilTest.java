package net.jr.util;

import org.junit.Assert;
import org.junit.Test;

public class HashUtilTest {

    @Test
    public void test() {
        Assert.assertEquals("8b1a9953c4611296a827abf8c47804d7", HashUtil.md5Hex("Hello".getBytes()));
        Assert.assertEquals("f7ff9e8b7bb2e09b70935a5d785e0cc5d9d0abf0", HashUtil.sha1Hex("Hello".getBytes()));
        Assert.assertEquals("185f8db32271fe25f561a6fc938b2e264306ec304eda518007d1764826381969", HashUtil.sha256Hex("Hello".getBytes()));
        Assert.assertEquals("70bc18bef5ae66b72d1995f8db90a583a60d77b4066e4653f1cead613025861c", HexUtil.bytesToHex(HashUtil.sha256Twice("Hello".getBytes())));
    }

}
