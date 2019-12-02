package net.jr.caching;

import org.junit.Assert;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MapCacheTest {

    @Test
    public void test() {
        Random r = new Random();
        int val = r.nextInt();
        MapCache<String, byte[]> c = new MapCache<>(10, TimeUnit.SECONDS);
        c.put("Data", Integer.toString(val).getBytes());
        String s = new String(c.get("Data"));
        Assert.assertEquals(Integer.toString(val), s);
        c.evict("Data");
        Assert.assertNull(c.get("Data"));
        c.evictAll();
    }
}
