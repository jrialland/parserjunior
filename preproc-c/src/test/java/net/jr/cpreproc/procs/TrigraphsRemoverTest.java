package net.jr.cpreproc.procs;

import net.jr.cpreproc.pipe.Suppliers;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TrigraphsRemoverTest {


    @Test
    public void testNoTrigraphs() {
        List<PreprocessorLine> list = Suppliers.fromString("Hello,\nworld2")
                .pipeTo(new TrigraphsRemover())
                .list();

        Assert.assertEquals(2, list.size());

        Assert.assertEquals("Hello,", list.get(0).getText());
        Assert.assertEquals("world2", list.get(1).getText());

    }

    @Test
    public void testDigraphs() {
        List<PreprocessorLine> list = Suppliers.fromString("int arr<:1:> = <%1%>")
                .pipeTo(new TrigraphsRemover())
                .list();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals("int arr[1] = {1}", list.get(0).getText());
    }
}
