package net.jr.util.table;

import org.junit.Assert;
import org.junit.Test;

public class AsciiTableViewTest {

    @Test
    public void testEmpty() {
        TableModel<Character> tm = new TableModel<>();
        String s = new AsciiTableView().tableToString(tm);
        Assert.assertEquals("┏┓\n┃┃\n┗┛\n", s);
    }

    @Test
    public void test2() {
        TableModel<Character> tm = new TableModel<>();
        char[] blacks = "♖♘♗♕♔♗♘♖".toCharArray();
        char[] whites = "♜♞♝♛♚♝♞♜".toCharArray();
        for (int x = 0; x < 8; x++) {
            tm.setData(x, 0, blacks[x]);
            tm.setData(x, 1, '♙');
            tm.setData(x, 6, '♟');
            tm.setData(x, 7, whites[x]);

        }
        String s = new AsciiTableView(3, 3).tableToString(tm);
        System.out.println(s);


        Assert.assertEquals("┏━━━┯━━━┯━━━┯━━━┯━━━┯━━━┯━━━┯━━━┓\n" +
                "┃ ♖ │ ♘ │ ♗ │ ♕ │ ♔ │ ♗ │ ♘ │ ♖ ┃\n" +
                "┠───┼───┼───┼───┼───┼───┼───┼───┨\n" +
                "┃ ♙ │ ♙ │ ♙ │ ♙ │ ♙ │ ♙ │ ♙ │ ♙ ┃\n" +
                "┠───┼───┼───┼───┼───┼───┼───┼───┨\n" +
                "┃   │   │   │   │   │   │   │   ┃\n" +
                "┠───┼───┼───┼───┼───┼───┼───┼───┨\n" +
                "┃   │   │   │   │   │   │   │   ┃\n" +
                "┠───┼───┼───┼───┼───┼───┼───┼───┨\n" +
                "┃   │   │   │   │   │   │   │   ┃\n" +
                "┠───┼───┼───┼───┼───┼───┼───┼───┨\n" +
                "┃   │   │   │   │   │   │   │   ┃\n" +
                "┠───┼───┼───┼───┼───┼───┼───┼───┨\n" +
                "┃ ♟ │ ♟ │ ♟ │ ♟ │ ♟ │ ♟ │ ♟ │ ♟ ┃\n" +
                "┠───┼───┼───┼───┼───┼───┼───┼───┨\n" +
                "┃ ♜ │ ♞ │ ♝ │ ♛ │ ♚ │ ♝ │ ♞ │ ♜ ┃\n" +
                "┗━━━┷━━━┷━━━┷━━━┷━━━┷━━━┷━━━┷━━━┛\n", s);

    }
}
