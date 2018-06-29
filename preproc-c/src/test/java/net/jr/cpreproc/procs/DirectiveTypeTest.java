package net.jr.cpreproc.procs;

import org.junit.Assert;
import org.junit.Test;

public class DirectiveTypeTest {

    @Test
    public void testDetectDirective() {
        Assert.assertEquals(DirectiveType.Define, DirectiveType.detectDirective("#define FOO BAR"));
    }
}
