package net.jr.collection.iterators;

import java.util.Iterator;
import java.util.Stack;

public final class Iterators {

    public static <T> PushbackIterator<T> pushbackIterator(Iterator<T> it) {

        if (it == null) {
            return null;
        }

        if (it instanceof PushbackIterator) {
            return (PushbackIterator<T>) it;
        }

        return new PushbackIterator<T>() {

            private boolean go = true;

            private Stack<T> stack = new Stack<>();

            @Override
            public void pushback(T item) {
                stack.push(item);
            }

            @Override
            public boolean hasNext() {
                return !stack.isEmpty() ? true : it.hasNext();
            }

            @Override
            public T next() {
                return !stack.isEmpty() ? stack.pop() : it.next();
            }
        };
    }
}
