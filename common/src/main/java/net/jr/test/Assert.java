package net.jr.test;

public class Assert {

    public static void notNull(Object obj, String msg) {
        if(obj == null) {
            throw new IllegalStateException(msg);
        }
    }

}
