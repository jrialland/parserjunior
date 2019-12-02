package net.jr.converters;

public interface Converter<A, B> {

    B convert(A a);

    A convertBack(B b);

}
