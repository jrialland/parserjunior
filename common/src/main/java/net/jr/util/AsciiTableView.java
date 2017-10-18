package net.jr.util;


import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

public class AsciiTableView {

    private int minCellWidth;

    private int maxCellWidth;

    public AsciiTableView() {
        this(0, Integer.MAX_VALUE);
    }

    public AsciiTableView(int minCellWidth, int maxCellWidth) {
        this.minCellWidth = Math.min(minCellWidth, maxCellWidth);
        this.maxCellWidth = Math.max(minCellWidth, maxCellWidth);
    }

    private Map<Integer, Integer> getColWidths(TableModel<?> tableModel) {
        final Map<Integer, Integer> colWidths = new TreeMap<Integer, Integer>();
        int maxX = tableModel.getMaxX();
        int maxY = tableModel.getMaxY();
        for (int i = 0; i <= maxX; i++) {
            int m = minCellWidth;
            for (int j = 0; j <= maxY; j++) {
                Object data = tableModel.getData(i, j);
                int len = data == null ? 0 : data.toString().length();
                m = Math.min(maxCellWidth, Math.max(m, len));
            }
            colWidths.put(i, m);
        }
        return colWidths;
    }

    private void makeSepLine(Writer writer, int nColumns, Function<Integer, Integer> getColumnWidth, char[] chars) throws IOException {
        writer.append(chars[0]);
        for (int i = 0; i <= nColumns; i++) {
            char[] s = new char[getColumnWidth.apply(i)];
            Arrays.fill(s, chars[1]);
            writer.append(new String(s));
            if (i != nColumns) {
                writer.append(chars[2]);
            }
        }
        writer.append(chars[3]);
        writer.append("\n");
    }

    private void makeDataLine(Writer writer, int row, TableModel<?> tableModel, Function<Integer, Integer> getColumnWidth, char[] chars) throws IOException {
        int maxX = tableModel.getMaxX();
        writer.append(chars[0]);
        for (int i = 0; i <= maxX; i++) {
            int colWidth = getColumnWidth.apply(i);
            Object data = tableModel.getData(i, row);
            String s = data == null ? "" : data.toString().trim().replaceAll("\r?\n", "");
            if (s.length() < colWidth) {
                while (s.length() != colWidth) s += " ";
            } else {
                s = s.substring(0, Math.min(s.length(), colWidth));
            }
            writer.append(s);
            if (i != maxX) {
                writer.append(chars[1]);
            }
        }
        writer.append(chars[2]);
        writer.append("\n");
    }

    public static String tableToString(TableModel<?> tableModel) {
        StringWriter sw = new StringWriter();
        try {
            new AsciiTableView().show(tableModel, sw);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sw.toString();
    }

    public <D> void show(TableModel<D> tableModel, Writer writer) throws IOException {
        int maxX = tableModel.getMaxX();
        int maxY = tableModel.getMaxY();
        Function<Integer, Integer> getColumnWidth;
        if (minCellWidth == maxCellWidth) {
            getColumnWidth = (i) -> minCellWidth;
        } else {
            final Map<Integer, Integer> colWidths = getColWidths(tableModel);
            getColumnWidth = (i) -> colWidths.get(i);
        }
        makeSepLine(writer, maxX, getColumnWidth, new char[]{'┏', '━', '┯', '┓'});
        for (int j = 0; j <= maxY; j++) {
            makeDataLine(writer, j, tableModel, getColumnWidth, new char[]{'┃', '│', '┃'});
            if (j != maxY) {
                makeSepLine(writer, maxX, getColumnWidth, new char[]{'┠', '─', '┼', '┨'});
            }
        }
        makeSepLine(writer, maxX, getColumnWidth, new char[]{'┗', '━', '┷', '┛'});
        writer.flush();
    }
}
