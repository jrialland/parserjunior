package net.jr.collection.iterators;

import java.util.Iterator;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;

public final class Iterators {

    public static <T> PushbackIterator<T> pushbackIterator(Iterator<T> it) {

        if (it == null) {
            return null;
        }

        if (it instanceof PushbackIterator) {
            return (PushbackIterator<T>) it;
        }

        return new PushbackIterator<T>() {

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

    public static <A, B> Iterator<B> convert(Iterator<A> it, Function<A, B> converter) {
        return new Iterator<B>() {
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public B next() {
                return converter.apply(it.next());
            }

            @Override
            public void remove() {
                it.remove();
            }

            @Override
            public void forEachRemaining(Consumer<? super B> consumer) {
                it.forEachRemaining(a -> consumer.accept(converter.apply(a)));
            }
        };
    }
}
