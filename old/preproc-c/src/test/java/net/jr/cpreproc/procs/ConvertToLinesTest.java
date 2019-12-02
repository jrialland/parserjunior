package net.jr.cpreproc.procs;

import net.jr.common.Position;
import net.jr.cpreproc.pipe.Suppliers;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class ConvertToLinesTest {

    @Test
    public void test() {

        ConvertToLines convertToLines = new ConvertToLines();


        List<PreprocessorLine> lines = Suppliers.fromString("This\nis\na\ttest")
                .pipeTo(convertToLines)
                .list();

        Assert.assertEquals(3, lines.size());

        Assert.assertEquals("This", lines.get(0).getText());
        Assert.assertEquals("is", lines.get(1).getText());
        Assert.assertEquals("a\ttest", lines.get(2).getText());

        Position p;

        p = lines.get(0).getPosition();
        Assert.assertEquals("1:1", p.toString());

        p = lines.get(1).getPosition();
        Assert.assertEquals("2:1", p.toString());

        p = lines.get(2).getPosition();
        Assert.assertEquals("3:1", p.toString());
    }
}
