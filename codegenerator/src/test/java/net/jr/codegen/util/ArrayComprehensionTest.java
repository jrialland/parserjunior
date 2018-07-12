package net.jr.codegen.util;

import org.junit.Test;

public class ArrayComprehensionTest {

    @Test
    public void test() {
        String s = ArrayComprehension.toJs(new int[]{17, 73, 54, 17,73, 14, 17});
        System.out.println(s);
    }

}
