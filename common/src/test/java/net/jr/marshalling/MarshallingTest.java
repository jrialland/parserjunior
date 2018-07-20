package net.jr.marshalling;

import org.junit.Assert;
import org.junit.Test;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;

public class MarshallingTest {

    @SuppressWarnings("unchecked")
    protected <X> X doTest(Object obj) {
        doTest(obj, true);
        return doTest(obj, false);
    }

    @SuppressWarnings("unchecked")
    protected <X> X doTest(Object obj, boolean compress) {
        byte[] marshalled = MarshallingUtil.toByteArray(obj, compress);
        Object obj2 = MarshallingUtil.fromByteArray(marshalled, compress);
        if (obj == null) {
            Assert.assertNull(obj2);
        } else if (obj.getClass().isArray()) {
            Assert.assertTrue(Arrays.deepEquals((Object[]) obj, (Object[]) obj2));
        } else {
            Assert.assertEquals(obj, obj2);
        }
        return (X) obj2;
    }

    @Test
    public void testNull() {
        doTest(null);
    }

    @Test
    public void testString() {
        doTest("Hello world");
    }

    @Test
    public void testList() {
        List<Integer> list = new ArrayList<>(Arrays.asList(27, 13, 202, 14));
        doTest(list);
    }

    @Test
    public void testSet() {
        Set<String> set = new HashSet<>(Arrays.asList("this", "is", "a", "test"));
        doTest(set);
    }

    @Test
    public void testMap() {
        Map<Character, String> map = new HashMap<>();
        map.put('A', "apple");
        map.put('B', "banana");
        map.put('C', "carrot");
        map.put('D', "dill");
        map.put('E', "eggplant");
        doTest(map);
    }

    @Test
    public void testByte() {
        doTest((byte) 0xaa);
    }

    @Test
    public void testShort() {
        doTest((short) 52);
    }

    @Test
    public void testInt() {
        doTest(-615);
    }

    @Test
    public void testLong() {
        doTest(651146L);
    }

    @Test
    public void testFloat() {
        doTest(3.141593f);
    }

    @Test
    public void testDouble() {
        doTest(Math.PI);
    }

    @Test
    public void testChar() {
        doTest('x');
    }

    @Test
    public void testBoolean() {
        doTest(true);
        doTest(false);
    }

    @Test
    public void testObject() {
        TestObject obj = new TestObject(12, "Test");
        TestObject obj2 = doTest(obj);
        Assert.assertEquals(12, obj2.getA());
        Assert.assertEquals("Test", obj2.getS());
    }

    public static class TestObject implements MarshallingCapable {

        private int a;

        private String s;

        public TestObject(int a, String s) {
            this.a = a;
            this.s = s;
        }

        @SuppressWarnings("unused")
        public static  TestObject  unMarshall(java.io.DataInput in) throws IOException {
            int a = in.readInt();
            String s = in.readUTF();
            return new TestObject(a, s);
        }

        public int getA() {
            return a;
        }

        public String getS() {
            return s;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            if (!obj.getClass().equals(TestObject.class)) {
                return false;
            }

            TestObject o = (TestObject) obj;
            return o.a == a && o.s.equals(s);
        }

        @Override
        public int hashCode() {
            return a + s.hashCode();
        }

        @Override
        public void marshall(DataOutput out) throws IOException {
            out.writeInt(a);
            out.writeUTF(s);
        }
    }
}
