package net.jr.cpreproc.procs;

import net.jr.cpreproc.pipe.Suppliers;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class ContinuedLinesMergerTest {


    @Test
    public void test() {
        String t = "This are \\\ncontinued lines";
        List<PreprocessorLine> l = Suppliers.fromString(t).pipeTo(new ConvertToLines()).pipeTo(new ContinuedLinesMerger()).list();
        Assert.assertEquals(1, l.size());
        Assert.assertEquals("This are continued lines", l.get(0).getText());

        Assert.assertEquals("1:1", l.get(0).getPosition(0).toString());
        Assert.assertEquals("1:9", l.get(0).getPosition(8).toString());
        Assert.assertEquals("2:1", l.get(0).getPosition(9).toString());
    }
}
