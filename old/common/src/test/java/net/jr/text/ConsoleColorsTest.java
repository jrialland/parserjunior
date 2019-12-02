package net.jr.text;


import org.junit.Test;

import java.lang.reflect.Method;

import static net.jr.text.ConsoleColors.*;

public class ConsoleColorsTest {


    String[] styles = {
            "Black",
            "Red",
            "Green",
            "Blue",
            "Purple",
            "Cyan",
            "White",
            "Bold",
            "Italics",
            "Underline",
    };

    @Test
    public void test() throws Exception {
        for (String style : styles) {
            Method m = ConsoleColors.class.getMethod(style.toLowerCase(), String.class);
            String s = (String) m.invoke(null, style);
            System.out.println(s);
        }
    }

    @Test
    public void testFancy() {
        System.out.println(bold(italics(underline(green("Fancy Text")))));
    }

}
