package net.jr.collection.iterators;

import java.util.Iterator;

public interface PushbackIterator<E> extends Iterator<E> {

    void pushback(E item);
}
