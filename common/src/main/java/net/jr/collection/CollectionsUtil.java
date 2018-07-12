package net.jr.collection;

import net.jr.collection.iterators.Iterators;
import net.jr.collection.iterators.PushbackIterator;

import java.lang.reflect.Array;
import java.util.*;

public final class CollectionsUtil {

    public static <K, V> Map<K, V> zip(K[] keys, V[] values) {
        if (keys.length != values.length) {
            throw new IllegalArgumentException("keys.length != values.length");
        }
        HashMap<K, V> map = new HashMap<>(keys.length);
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], values[i]);
        }
        return map;
    }

    public static List<Integer> fromArray(int[] array) {
        List<Integer> list = new ArrayList<>(array.length);
        for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
        }
        return list;
    }

    public static List<Boolean> fromArray(boolean[] array) {
        List<Boolean> list = new ArrayList<>(array.length);
        for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
        }
        return list;
    }

    public static List<Character> fromArray(char[] array) {
        List<Character> list = new ArrayList<>(array.length);
        for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
        }
        return list;
    }

    public static List<Short> fromArray(short[] array) {
        List<Short> list = new ArrayList<>(array.length);
        for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
        }
        return list;
    }

    public static List<Float> fromArray(float[] array) {
        List<Float> list = new ArrayList<>(array.length);
        for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
        }
        return list;
    }

    public static List<Double> fromArray(double[] array) {
        List<Double> list = new ArrayList<>(array.length);
        for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
        }
        return list;
    }


    public static <T> List<T> prependedList(T head, List<T> tail) {
        return Collections.unmodifiableList(new List<T>() {
            @Override
            public int size() {
                return 1 + tail.size();
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean contains(Object o) {
                if (o == null) {
                    return head == null || tail.contains(o);
                } else {
                    return (head != null && o.equals(head)) || tail.contains(o);
                }
            }

            @Override
            public Iterator<T> iterator() {
                PushbackIterator<T> it = Iterators.pushbackIterator(tail.iterator());
                it.pushback(head);
                return it;
            }

            @Override
            public Object[] toArray() {
                return new Object[0];
            }

            @Override
            public <T1> T1[] toArray(T1[] t1s) {
                T1[] a = (T1[]) Array.newInstance(t1s.getClass().getComponentType(), size());
                a[0] = (T1) head;
                int i = 1;
                for (T item : tail) {
                    a[i++] = (T1) item;
                }
                return a;
            }

            @Override
            public boolean add(T t) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean remove(Object o) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean containsAll(Collection<?> collection) {
                for (Object obj : collection) {
                    if (!contains(obj)) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public boolean addAll(Collection<? extends T> collection) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean addAll(int i, Collection<? extends T> collection) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean removeAll(Collection<?> collection) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean retainAll(Collection<?> collection) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void clear() {
                throw new UnsupportedOperationException();
            }

            @Override
            public T get(int i) {
                return i == 0 ? head : tail.get(i - 1);
            }

            @Override
            public T set(int i, T t) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void add(int i, T t) {
                throw new UnsupportedOperationException();
            }

            @Override
            public T remove(int i) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int indexOf(Object o) {
                return 0;
            }

            @Override
            public int lastIndexOf(Object o) {
                return 0;
            }

            @Override
            public ListIterator<T> listIterator() {
                return listIterator(0);
            }

            @Override
            public ListIterator<T> listIterator(final int i) {
                return new ListIterator<T>() {

                    private int index = i;

                    @Override
                    public boolean hasNext() {
                        return index == size();
                    }

                    @Override
                    public T next() {
                        return get(index++);
                    }

                    @Override
                    public boolean hasPrevious() {
                        return index > 0;
                    }

                    @Override
                    public T previous() {
                        return get(index--);
                    }

                    @Override
                    public int nextIndex() {
                        return index + 1;
                    }

                    @Override
                    public int previousIndex() {
                        return index - 1;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public void set(T t) {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public void add(T t) {
                        throw new UnsupportedOperationException();
                    }
                };
            }

            @Override
            public List<T> subList(int i, int i1) {
                if (i == 0) {
                    return CollectionsUtil.prependedList(head, tail.subList(0, i1 - 1));
                } else {
                    return tail.subList(i - 1, i1 - 1);
                }
            }
        });
    }

}