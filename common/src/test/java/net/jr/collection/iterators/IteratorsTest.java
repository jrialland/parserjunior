package net.jr.collection.iterators;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class IteratorsTest {

    @Test
    public void testPushbackIterator() {
        List<String> list = new ArrayList<>(Arrays.asList("A", "C"));
        PushbackIterator<String> pbit = Iterators.pushbackIterator(list.iterator());
        Assert.assertEquals("A", pbit.next());
        pbit.pushback("B");
        Assert.assertTrue(pbit.hasNext());
        Assert.assertEquals("B", pbit.next());
        Assert.assertEquals("C", pbit.next());
        Assert.assertFalse(pbit.hasNext());
    }

    @Test
    public void testNull() {
        Assert.assertEquals(null, Iterators.pushbackIterator(null));
    }

    @Test
    public void testEq() {
        List<String> list = new ArrayList<>(Arrays.asList("A", "C"));
        PushbackIterator<String> pbit = Iterators.pushbackIterator(list.iterator());
        Assert.assertEquals(pbit, Iterators.pushbackIterator(pbit));
    }

    @Test
    public void testConvert() {

        List<String> list = new ArrayList<>(Arrays.asList("1", "2", "3", "4"));

        Iterator<String> it = list.iterator();

        Iterator<Integer> it2 = Iterators.convert(it, s -> Integer.parseInt(s));


        Assert.assertTrue(it2.hasNext());
        Assert.assertEquals(1, (int) it2.next());

        it2.remove();

        int[] sum = new int[]{0};
        it2.forEachRemaining(i -> sum[0] += i);
        Assert.assertEquals(9, sum[0]);

        Assert.assertEquals("[2, 3, 4]", list.toString());
    }
}
