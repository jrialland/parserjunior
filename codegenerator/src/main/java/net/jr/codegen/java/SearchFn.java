package net.jr.codegen.java;

import net.jr.text.IndentPrintWriter;

import java.io.Writer;
import java.util.*;

public class SearchFn {

    private static class Xyz {
        private int x, y, z;
        public Xyz(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }


    private interface Zone {
        Zone extend(int x, int y);
    }

    private static class Cell implements Zone {

        private int x, y;

        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Zone extend(int x, int y) {
            if(x == this.x) {
                if(y == this.y) {
                    return this;
                }
            }
            if(y == this.y) {

            }

        }

    }

    private class Rect implements Zone {

        private int x1, y1, x2, y2;

        public Zone extend(int x, int y) {
            if()
        }
    }

    private static class Region {

        private int z;

        private List<Zone> zones = new ArrayList<>();

        public Region(int x, int y, int z) {
            zones.add(new Cell(x, y));
            this.z = z;
        }

        public void extend(List<Xyz> all, int x, int y) {

        }

    }

    public static void generate(List<Integer> lx, List<Integer> ly, List<Integer> lz, Writer writer) {
        int len = lx.size();
        assert len == ly.size() && len == lz.size();

        Map<Integer, Region> byZ = new HashMap<>();
        Iterator<Integer> itX = lx.iterator();
        Iterator<Integer> itY = ly.iterator();
        Iterator<Integer> itZ = lz.iterator();
        List<Xyz> lxyz = new ArrayList<>(len);

        int i = 0;
        while(i<len) {
            lxyz.add(new Xyz(itX.next(), itY.next(), itZ.next()));
        }

        Collections.sort(lxyz, Comparator.comparingInt(c->c.x));
        lxyz.stream().forEach((c -> byZ.computeIfAbsent(c.z, z->new Region(c.x, c.y, c.z)).extend(lxyz, c.x, c.y)));
        IndentPrintWriter ipw = new IndentPrintWriter(writer);
        ipw.flush();
    }
}
