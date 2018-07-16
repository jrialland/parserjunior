package net.jr.codegen.java;

import java.io.Writer;
import java.util.*;

public class SearchFn {

    static class Link {
        private Node from;
        private Integer x, y;
        static Link whenDefineX( Node from, int x) {
            Link l = new Link();
            l.x = x;
            l.from = from;
            return l;
        }
        static Link whenDefineY( Node from, int y) {
            Link l = new Link();
            l.y = y;
            l.from = from;
            return l;
        }

        @Override
        public String toString() {
            String var = x!= null ? "x" : "y";
            return "define " + var + " " + (x!=null?x:y)+ ", " + from.toString();
        }
    }

    static class Node {

        private Set<Link> from;

        private Integer x, y, z;

        static Node forX(int x) {
            Node n = new Node();
            n.from = fromStartNode;
            n.x = x;
            return n;
        }

        static Node forY(int y) {
            Node n = new Node();
            n.from = fromStartNode;
            n.y = y;
            return n;
        }

        static Node forZ(int z) {
            Node n = new Node();
            n.z = z;
            n.from = new HashSet<>();
            return n;
        }

        @Override
        public String toString() {
            String var = "???";
            int val = 0;
            if(x != null) {
                var = "x";
                val = x;
            }
            else if(x != null) {
                var = "y";
                val = y;
            }
            else if(z != null) {
                var = "z";
                val = z;
            }
            return "define " + var + " " + val;
        }
    }

    static Node START_NODE = new Node();

    static Set<Link> fromStartNode = new HashSet<>(Arrays.asList(new Link()));
    static {
        fromStartNode.iterator().next().from = START_NODE;
    }

    public static void generate(List<Integer> lx, List<Integer> ly, List<Integer> lz, Writer writer) {
        assert lx.size() == ly.size();
        assert lx.size() == lz.size();

        Iterator<Integer> itX = lx.iterator();
        Iterator<Integer> itY = ly.iterator();
        Iterator<Integer> itZ = lz.iterator();


        Map<String, Node> nodes = new TreeMap<>();
        Set<Node> zSet = new HashSet<>();

        for(int i=0, max = lx.size(); i<max; i++) {

            int x = itX.next();
            int y = itY.next();
            int z = itZ.next();

            Node nx = nodes.computeIfAbsent("x"+x, k -> Node.forX(x));
            Node ny = nodes.computeIfAbsent("y"+y, k -> Node.forY(y));
            Node nz = nodes.computeIfAbsent("z"+z, k -> Node.forZ(z));
            nz.from.add(Link.whenDefineX(ny, x));
            nz.from.add(Link.whenDefineY(nx, y));
            zSet.add(nz);
        }

        List<Node> zList = new ArrayList<>(zSet);
        Collections.sort(zList, Comparator.comparingInt(n -> n.z));

        for(Node zn : zList) {
            for(Link l : zn.from){
                System.out.println(zn.z + "  <=  " + l.toString());
            }
        }
    }
}
