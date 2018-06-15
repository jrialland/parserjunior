package net.jr.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.stream.Stream;

public class StreamUtilTest {

    @Test
    public void testTakeWhile() {
        Stream<Character> stream = Arrays.asList('h', 'e', 'l', 'l', 'o', '!').stream();
        StringWriter sw = new StringWriter();
        StreamUtil.takeWhile(stream, c -> c != '!').forEach( c -> sw.append(c));
        Assert.assertEquals("hello", sw.toString());
    }

}
