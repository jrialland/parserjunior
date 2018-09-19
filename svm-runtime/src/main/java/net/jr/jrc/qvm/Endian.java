
package net.jr.jrc.qvm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class Endian {

  /**
   * read 4 bytes
   * 
   * @param val
   *          a 4 bytes array
   * @return the corresponding int value, interpreted as a signed LE int
   */
  public static int read4Le(byte[] val) {
    ByteBuffer bb = ByteBuffer.allocate(4);
    bb.order(ByteOrder.LITTLE_ENDIAN);
    bb.put(val);
    bb.flip();
    return bb.getInt();
  }

  public static int read4Le(InputStream is) throws IOException {
    ByteBuffer bb = ByteBuffer.allocate(4);
    bb.order(ByteOrder.LITTLE_ENDIAN);
    for (int i = 0; i < 4; i++) {
      bb.put((byte) is.read());
    }
    bb.flip();
    return bb.getInt();
  }

  public static short read2Le(byte[] val) {
    ByteBuffer bb = ByteBuffer.allocate(4);
    bb.order(ByteOrder.LITTLE_ENDIAN);
    bb.put(val);
    bb.flip();
    return bb.getShort();
  }

  public static float read4Lef(byte[] val) {
    return Float.intBitsToFloat(read4Le(val));
  }

  public static void write4Le(int val, byte[] bytes) {
    ByteBuffer bb = ByteBuffer.allocate(4);
    bb.order(ByteOrder.LITTLE_ENDIAN);
    bb.putInt(val);
    System.arraycopy(bb.array(), bb.arrayOffset(), bytes, 0, 4);
  }

  public static void write4Le(int val, OutputStream os) throws IOException {
    ByteBuffer bb = ByteBuffer.allocate(4);
    bb.order(ByteOrder.LITTLE_ENDIAN);
    bb.putInt(val);
    os.write(bb.array(), bb.arrayOffset(), 4);
  }

  public static void write2Le(short val, byte[] bytes) {
    ByteBuffer bb = ByteBuffer.allocate(4);
    bb.order(ByteOrder.LITTLE_ENDIAN);
    bb.putShort(val);
    System.arraycopy(bb.array(), bb.arrayOffset(), bytes, 0, 2);
  }

  public static void write4Lef(float val, byte[] bytes) {
    write4Le(Float.floatToRawIntBits(val), bytes);
  }

  public static void write4Lef(float val, OutputStream os) throws IOException {
    write4Le(Float.floatToRawIntBits(val), os);
  }

  /**
   * Read 4 bytes as an unsigned int. Because unsigned ints cannot be
   * represented in java, the value is returned as a long
   * 
   * @param val
   *          a 4 bytes array
   * @return the corresponding int value, interpreted as a signed LE int
   */
  public static long read4ULe(byte[] val) {
    long v = read4Le(val);
    return v & 0X00000000FFFFFFFFL;
  }

  public static void write4ULe(long val, byte[] bytes) {
    if (val > Integer.MAX_VALUE) {
      write4Le(((int) val) & 0xFFFFFFFF, bytes);
    } else {
      write4Le((int) val, bytes);
    }
  }

}
