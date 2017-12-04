package net.jr.collection.iterators;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
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
}
