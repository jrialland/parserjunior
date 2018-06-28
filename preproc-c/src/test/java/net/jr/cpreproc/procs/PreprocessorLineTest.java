package net.jr.cpreproc.procs;

import net.jr.common.Position;
import org.junit.Assert;
import org.junit.Test;

public class PreprocessorLineTest {

    @Test
    public void testInsert() {

        PreprocessorLine l = new PreprocessorLine(new Position(1, 1), "Helo");
        l.insert(2, "l");
        l.insert(5, " world");
        Assert.assertEquals("Hello world", l.getText());

        l.insert(0, ">>");
        Assert.assertEquals(">>Hello world", l.getText());

        l.insert(20, "!");
        Assert.assertEquals(">>Hello world       !", l.getText());

    }

    @Test
    public void testRemoveChars() {
        PreprocessorLine l = new PreprocessorLine(new Position(1, 1), "HellXYo");
        l.removeChars(4, 2);
        Assert.assertEquals("Hello", l.getText());
    }


}
