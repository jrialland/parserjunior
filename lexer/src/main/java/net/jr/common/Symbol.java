package net.jr.common;


import net.jr.marshalling.MarshallingCapable;

public interface Symbol extends MarshallingCapable {

    boolean isTerminal();

}
