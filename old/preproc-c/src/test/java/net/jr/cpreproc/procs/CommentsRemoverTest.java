package net.jr.cpreproc.procs;

import net.jr.common.Position;
import net.jr.cpreproc.pipe.Suppliers;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class CommentsRemoverTest {


    @Test
    public void testMultiline() {
        List<PreprocessorLine> list = Suppliers.fromString("This is a /* multiline \ncomment */ test")
                .pipeTo(new ConvertToLines())
                .pipeTo(new CommentsRemover())
                .list();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals("This is a  test", list.get(0).getText());
        Assert.assertEquals(new Position(1, 1), list.get(0).getPosition());
        Assert.assertEquals(new Position(2, 13), list.get(0).getPosition(12));
    }

    @Test
    public void testEolComments() {
        List<PreprocessorLine> list = Suppliers.fromString("this // has\ncomments// on\nevery // line\n")
                .pipeTo(new ConvertToLines())
                .pipeTo(new CommentsRemover())
                .list();
        Assert.assertEquals(3, list.size());
        Assert.assertEquals("this ", list.get(0).getText());
        Assert.assertEquals("comments", list.get(1).getText());
        Assert.assertEquals("every ", list.get(2).getText());
        Assert.assertEquals(new Position(1, 1), list.get(0).getPosition());
        Assert.assertEquals(new Position(2, 1), list.get(1).getPosition());
    }

    @Test
    public void testCommentsWithinStrings() {
        List<PreprocessorLine> list = Suppliers.fromString("this \"/*should not be removed*/\"")
                .pipeTo(new ConvertToLines())
                .pipeTo(new CommentsRemover())
                .list();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals("this \"/*should not be removed*/\"", list.get(0).getText());
    }

    @Test
    public void testComplicated() {
        List<PreprocessorLine> list = Suppliers.fromString("test(\"//string 1\", \"/*string 2*/\") /*/* /***/ ;//!!/**/\"")
                .pipeTo(new ConvertToLines())
                .pipeTo(new CommentsRemover())
                .list();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals("test(\"//string 1\", \"/*string 2*/\")  ;", list.get(0).getText());
    }
}