package net.jr.jrc.qvm.syscalls;

import net.jr.jrc.qvm.QvmInterpreter;

import java.util.Random;

public class Rand {

    public static final int RAND_MAX = 32767; // max for signed 32bits int

    private Random random = new Random();

    public int rand(QvmInterpreter interpreter) {
        return random.nextInt(RAND_MAX);
    }
}
