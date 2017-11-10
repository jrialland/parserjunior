package net.jr.util.table;

import java.util.*;
import java.util.stream.Collectors;

public class TableModel<DataType> {

    private class Coord {
        int x;
        int y;

        public Coord(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public int hashCode() {
            return x+y;
        }

        @Override
        public boolean equals(Object o) {
            final Coord c = (Coord)o;
            return x == c.x && y == c.y;
        }
    }

    private class Rectangle {
        int minx, miny, maxx, maxy;

        public Rectangle(int minx, int miny, int maxx, int maxy) {
            this.minx = minx;
            this.miny = miny;
            this.maxx = maxx;
            this.maxy = maxy;
        }


        public boolean contains(int x, int y) {
            return x>=minx && x<=maxx && y>=miny && y<=maxy;
        }
    }

    private class StyleRule {
        private Rectangle zone;
        private String cssKey;
        private String cssValue;
        private boolean remove = false;
    }

    private List<StyleRule> styleRules = new ArrayList<>();

    Map<Coord, DataType> tableData = new HashMap<>();

    public void setData(int x, int y, DataType data) {
        tableData.put(new Coord(x,y), data);
    }

    public DataType getData(int x, int y) {
        return tableData.get(new Coord(x,y));
    }

    private StyleRule makeRule(int x1, int y1, int x2, int y2, String rule) {
        StyleRule sr = new StyleRule();
        sr.zone = new Rectangle(Math.min(x1,x2), Math.min(y1, y2), Math.max(x1, x2), Math.max(y1, y2));
        rule = rule.trim().replaceFirst(";*$","");
        String[] parts = rule.split(":");
        if(parts.length != 2) {
            throw new IllegalArgumentException("Could not parse '"+rule+"'");
        }
        sr.cssKey = parts[0].trim().toLowerCase();
        sr.cssValue = parts[1].trim();
        return sr;
    }

    public void addStyleHint(int x1, int y1, int x2, int y2, String rule) {
        styleRules.add(makeRule(x1, y1, x2, y2, rule));
    }

    public void removeStyleHint(int x1, int y1, int x2, int y2, String rule) {
        StyleRule sr = new StyleRule();
        sr.remove = true;
        styleRules.add(sr);
    }

    public String getStyle(int x, int y) {
        Map<String, String> rules = new TreeMap<>();
        for(StyleRule sr : styleRules) {
            if(sr.zone.contains(x, y)) {
                if(sr.remove) {
                    rules.remove(sr.cssKey);
                } else {
                    rules.put(sr.cssKey, sr.cssValue);
                }
            }
        }
        return String.join(";\n", rules.entrySet().stream().map(e -> e.getKey()+":"+e.getValue()).collect(Collectors.toList()));
    }

    public int getMaxX() {
        Optional<Integer> oMax = tableData.keySet().stream().map(c -> c.x).max(Integer::compareTo);
        return oMax.isPresent()?oMax.get():0;
    }


    public int getMaxY() {
        Optional<Integer> oMax = tableData.keySet().stream().map(c -> c.y).max(Integer::compareTo);
        return oMax.isPresent()?oMax.get():0;
    }

    public void addStyleOnRow(int rowIndex, String rule) {
        addStyleHint(0, rowIndex, Integer.MAX_VALUE, rowIndex,rule);
    }

    public void removeStyleFromRow(int rowIndex, String rule) {
        removeStyleHint(0, rowIndex, Integer.MAX_VALUE, rowIndex, rule);

    }

    public void addStyleOnColumn(int columnIndex, String rule) {
        addStyleHint(columnIndex, 0 ,columnIndex, Integer.MAX_VALUE, rule);
    }

    public void removeStyleFromColumn(int columnIndex, String rule) {
        removeStyleHint(columnIndex, 0 ,columnIndex, Integer.MAX_VALUE, rule);
    }

    private Coord moveBy(Coord c, int dx, int dy) {
        return new Coord(c.x + dx, c.y + dy);
    }

    public void moveDataBy(int dx, int dy) {
        tableData = tableData.entrySet().stream()
                .collect(Collectors.toMap(e -> moveBy(e.getKey(), dx, dy), e -> e.getValue()));
    }
}
