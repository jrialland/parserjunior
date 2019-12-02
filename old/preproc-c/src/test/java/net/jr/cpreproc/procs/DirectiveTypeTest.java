package net.jr.cpreproc.procs;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

public class DirectiveTypeTest {

    @Test
    public void testDetectDirective() {
        Pair<DirectiveType, String> detected = DirectiveType.detectDirective("#define FOO BAR");
        Assert.assertEquals(DirectiveType.Define, detected.getKey());
        Assert.assertEquals("FOO BAR", detected.getValue());
    }
}
