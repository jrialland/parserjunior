package net.jr.marshalling;

import net.jr.converters.Converter;
import net.jr.types.TypeUtil;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class MarshallingUtil {

    private static final char NULL = '0';

    private static final char LIST = 'l';

    private static final char SET = 's';

    private static final char MAP = 'm';

    private static final char STRING = 'z';

    private interface Marshaller {
        void marshall(Object obj, DataOutputStream dataOutputStream) throws IOException;
    }

    private interface UnMarshaller {
        Object unMarshall(DataInputStream dataInputStream) throws IOException;
    }

    private static final Map<Character, Marshaller> marshallers = new TreeMap<>();

    private static final Map<Character, UnMarshaller> unMarshallers = new TreeMap<>();

    static {

        marshallers.put(NULL, (obj, out) -> {
            out.writeChar(NULL);
        });

        unMarshallers.put(NULL, in -> null);

        marshallers.put(TypeUtil.ARRAY, (obj, out) -> {
            out.writeChar(TypeUtil.ARRAY);
            int len = Array.getLength(obj);
            out.writeInt(len);
            out.writeUTF(TypeUtil.getBytecodeTypename(obj.getClass().getComponentType()));
            for (int i = 0; i < len; i++) {
                marshall(Array.get(obj, i), out);
            }
        });

        unMarshallers.put(TypeUtil.ARRAY, (in) -> {
            int len = in.readInt();
            Class<?> componentType = TypeUtil.forBytecodeTypename(in.readUTF());
            Object array = Array.newInstance(componentType, len);
            for (int i = 0; i < len; i++) {
                Array.set(array, i, unMarshall(in));
            }
            return array;
        });

        marshallers.put(LIST, (obj, out) -> {
            List<?> list = (List<?>) obj;
            out.writeChar(LIST);
            out.writeInt(list.size());
            for (Object item : list) {
                marshall(item, out);
            }
        });

        unMarshallers.put(LIST, (in) -> {
            int size = in.readInt();
            List<?> list = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                list.add(unMarshall(in));
            }
            return list;
        });

        marshallers.put(SET, (obj, out) -> {
            Set<?> set = (Set<?>) obj;
            out.writeChar(SET);
            out.writeInt(set.size());
            for (Object item : set) {
                marshall(item, out);
            }
        });

        unMarshallers.put(SET, (in) -> {
            int size = in.readInt();
            Set<?> set = new HashSet<>();
            for (int i = 0; i < size; i++) {
                set.add(unMarshall(in));
            }
            return set;
        });

        marshallers.put(MAP, (obj, out) -> {
            Map<?, ?> map = (Map) obj;
            out.writeChar(MAP);
            out.writeInt(map.size());
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                marshall(entry.getKey(), out);
                marshall(entry.getValue(), out);
            }
        });

        unMarshallers.put(MAP, (in) -> {
            int size = in.readInt();
            Map<Object, Object> map = new HashMap<>();
            for (int i = 0; i < size; i++) {
                Object k = unMarshall(in);
                Object v = unMarshall(in);
                map.put(k, v);
            }
            return map;
        });

        marshallers.put(TypeUtil.OBJECT, (obj, out) -> {
            MarshallingCapable m = (MarshallingCapable) obj;
            out.writeChar(TypeUtil.OBJECT);
            out.writeUTF(TypeUtil.getBytecodeTypename(obj.getClass()));
            m.marshall(out);
        });

        marshallers.put(STRING, (obj, out) -> {
            out.writeChar(TypeUtil.OBJECT);
            out.writeUTF("Ljava/lang/String;");
            out.writeUTF(obj.toString());
        });

        unMarshallers.put(TypeUtil.OBJECT, (in) -> {
            String className = in.readUTF();
            if (className.equals("Ljava/lang/String;")) {
                return in.readUTF();
            } else {
                try {
                    Class<?> clazz = TypeUtil.forBytecodeTypename(className);
                    Method method = clazz.getMethod("unMarshall", DataInputStream.class);
                    method.setAccessible(true);
                    if (!Modifier.isStatic(method.getModifiers())) {
                        throw new IllegalStateException(String.format("The %s::%s method must be static", clazz.getName(), method.getName()));
                    }
                    return method.invoke(null, in);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

        });

        marshallers.put(TypeUtil.BYTE, (obj, out) -> {
            out.writeChar(TypeUtil.BYTE);
            out.writeByte((Byte) obj);
        });

        unMarshallers.put(TypeUtil.BYTE, (in) -> in.readByte());

        marshallers.put(TypeUtil.SHORT, (obj, out) -> {
            out.writeChar(TypeUtil.SHORT);
            out.writeShort((Short) obj);
        });

        unMarshallers.put(TypeUtil.SHORT, in -> in.readShort());

        marshallers.put(TypeUtil.INT, (obj, out) -> {
            out.writeChar(TypeUtil.INT);
            out.writeInt((Integer) obj);
        });

        unMarshallers.put(TypeUtil.INT, in -> in.readInt());

        marshallers.put(TypeUtil.LONG, (obj, out) -> {
            out.writeChar(TypeUtil.LONG);
            out.writeLong((Long) obj);
        });

        unMarshallers.put(TypeUtil.LONG, in -> in.readLong());

        marshallers.put(TypeUtil.FLOAT, (obj, out) -> {
            out.writeChar(TypeUtil.FLOAT);
            out.writeFloat((Float) obj);
        });

        unMarshallers.put(TypeUtil.FLOAT, in -> in.readFloat());

        marshallers.put(TypeUtil.DOUBLE, (obj, out) -> {
            out.writeChar(TypeUtil.DOUBLE);
            out.writeDouble((Double) obj);
        });

        unMarshallers.put(TypeUtil.DOUBLE, in -> in.readDouble());

        marshallers.put(TypeUtil.BOOLEAN, (obj, out) -> {
            out.writeChar(TypeUtil.BOOLEAN);
            out.writeBoolean((Boolean) obj);
        });

        unMarshallers.put(TypeUtil.BOOLEAN, in -> in.readBoolean());

        marshallers.put(TypeUtil.CHAR, (obj, out) -> {
            out.writeChar(TypeUtil.CHAR);
            out.writeChar((Character) obj);
        });

        unMarshallers.put(TypeUtil.CHAR, in -> in.readChar());
    }

    public static void marshall(Object obj, DataOutputStream dataOutputStream) throws IOException {
        int type = -1;

        //NULL
        if (obj == null) {
            type = NULL;
        }

        //STRING
        else if (obj instanceof CharSequence) {
            type = STRING;
        }

        //ARRAY
        else if (obj.getClass().isArray()) {
            type = TypeUtil.ARRAY;
        }

        //LIST
        else if (obj instanceof List) {
            type = LIST;
        }

        //SET
        else if (obj instanceof Set) {
            type = SET;
        }

        //MAP
        else if (obj instanceof Map) {
            type = MAP;
        }

        //OBJECT
        else if (obj instanceof MarshallingCapable) {
            type = TypeUtil.OBJECT;
        }

        //BYTE
        else if (obj instanceof Byte) {
            type = TypeUtil.BYTE;
        }

        //SHORT
        else if (obj instanceof Short) {
            type = TypeUtil.SHORT;
        }

        //INT
        else if (obj instanceof Integer) {
            type = TypeUtil.INT;
        }

        //LONG
        else if (obj instanceof Long) {
            type = TypeUtil.LONG;
        }

        //FLOAT
        else if (obj instanceof Float) {
            type = TypeUtil.FLOAT;
        }

        //DOUBLE
        else if (obj instanceof Double) {
            type = TypeUtil.DOUBLE;
        }

        //BOOLEAN
        else if (obj instanceof Boolean) {
            type = TypeUtil.BOOLEAN;
        }

        //CHAR
        else if (obj instanceof Character) {
            type = TypeUtil.CHAR;
        }

        final Marshaller marshaller;
        if (type == -1 || (marshaller = marshallers.get((char) type)) == null) {
            throw new UnsupportedOperationException(String.format("Unmarshallable '%s'", obj == null ? "null" : obj.getClass().getName()));
        } else {
            marshaller.marshall(obj, dataOutputStream);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T unMarshall(DataInputStream dataInputStream) throws IOException {
        char type = dataInputStream.readChar();
        UnMarshaller unMarshaller = unMarshallers.get(type);
        if (unMarshaller == null) {
            throw new IllegalStateException(String.format("For type code '%s'", type));
        } else {
            return (T) unMarshaller.unMarshall(dataInputStream);
        }
    }

    public static byte[] toByteArray(Object obj, boolean compress) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStream out = baos;
            GZIPOutputStream gzOut = null;
            if (compress) {
                out = gzOut = new GZIPOutputStream(baos);
            }
            DataOutputStream dataOutputStream = new DataOutputStream(out);
            marshall(obj, dataOutputStream);
            dataOutputStream.flush();
            if (compress) {
                gzOut.finish();
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T fromByteArray(byte[] data, boolean decompress) {
        try {
            InputStream in = new ByteArrayInputStream(data);
            if (decompress) {
                in = new GZIPInputStream(in);
            }
            return unMarshall(new DataInputStream(in));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <A> Converter<A, byte[]> converter(Class<A> targetType, boolean compress) {
        return new Converter<A, byte[]>() {
            @Override
            public byte[] convert(A a) {
                return MarshallingUtil.toByteArray(a, compress);
            }

            @Override
            public A convertBack(byte[] bytes) {
                return MarshallingUtil.fromByteArray(bytes, compress);
            }
        };
    }

    public static <T> T copyOf(T obj) {
        return fromByteArray(toByteArray(obj, false), false);
    }
}
