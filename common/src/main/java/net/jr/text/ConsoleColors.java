package net.jr.text;

public class ConsoleColors {

    public static final String ANSI_RESET = "\u001B[0m";

    public static final String ANSI_BLACK = "\u001B[30m";

    public static final String ANSI_RED = "\u001B[31m";

    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";

    public static final String ANSI_BLUE = "\u001B[34m";

    public static final String ANSI_PURPLE = "\u001B[35m";

    public static final String ANSI_CYAN = "\u001B[36m";

    public static final String ANSI_WHITE = "\u001B[37m";

    public static final String ANSI_BOLD = "\u001B[1m";

    public static final String ANSI_ITALICS = "\u001B[3m";

    public static final String ANSI_UNDERLINE = "\u001B[4m";

    private static final String color(String txt, String colorCode) {
        return colorCode + txt + ANSI_RESET;
    }

    public static final String black(String txt) {
        return color(txt, ANSI_BLACK);
    }

    public static final String red(String txt) {
        return color(txt, ANSI_RED);
    }

    public static final String green(String txt) {
        return color(txt, ANSI_GREEN);
    }

    public static final String yellow(String txt) {
        return color(txt, ANSI_YELLOW);
    }

    public static final String blue(String txt) {
        return color(txt, ANSI_BLUE);
    }

    public static final String purple(String txt) {
        return color(txt, ANSI_PURPLE);
    }

    public static final String cyan(String txt) {
        return color(txt, ANSI_CYAN);
    }

    public static final String white(String txt) {
        return color(txt, ANSI_WHITE);
    }

    public static final String bold(String txt) {
        return color(txt, ANSI_BOLD);
    }

    public static final String italics(String txt) {
        return color(txt, ANSI_ITALICS);
    }

    public static final String underline(String txt) {
        return color(txt, ANSI_UNDERLINE);
    }
}
