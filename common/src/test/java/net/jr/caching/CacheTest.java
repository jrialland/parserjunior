package net.jr.caching;

import net.jr.marshalling.MarshallingCapable;
import net.jr.marshalling.MarshallingUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.DataOutput;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class CacheTest {

    private Cache<TestObj, TestObj> getCache() {

        Cache.Builder<String, byte[]> onDisk = Cache.Builder.onDisk(TestObj.class.getName())
                .withTtl(1, TimeUnit.HOURS);

        return Cache.Builder.inMemory(TestObj.class, TestObj.class)
                .withTtl(10, TimeUnit.MINUTES)
                .fallbackTo(
                        onDisk.withKeyMapper(TestObj::getId)
                                .withValueConverter(MarshallingUtil.converter(TestObj.class, true))
                )
                .withFactory(o -> new TestObj("v" + o.getId()))
                .build();
    }

    @Test
    public void doTest() {
        Cache<TestObj, TestObj> cache = getCache();
        cache.evictAll();
        TestObj k1 = new TestObj("1");
        TestObj v = cache.get(k1);
        Assert.assertEquals("v1", v.getId());
        cache.put(new TestObj("2"), new TestObj("v2"));
        cache.evict(k1);
        cache.evict(new TestObj("3"));
        cache.evictAll();
    }

    public static class TestObj implements MarshallingCapable {
        private String id;

        public TestObj(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        @Override
        public void marshall(DataOutput out) throws IOException {
            out.writeUTF(id);
        }
    }


}
