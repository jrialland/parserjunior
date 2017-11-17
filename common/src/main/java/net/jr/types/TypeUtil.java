package net.jr.types;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

public class TypeUtil {

    private static final Map<Class<?>, Character> basicTypes = new HashMap<>();

    private static final Map<Class<?>, Character> boxingTypes = new HashMap<>();

    private static final Map<Character, Class<?>> reverseBoxingTypes = new HashMap<>();

    public static final char BYTE = 'B';

    public static final char CHAR = 'C';

    public static final char DOUBLE = 'D';

    public static final char FLOAT = 'F';

    public static final char INT = 'I';

    public static final char LONG = 'J';

    public static final char SHORT = 'S';

    public static final char BOOLEAN = 'Z';

    public static final char OBJECT = 'L';

    public static final char ARRAY = '[';

    static {
        //B = byte
        basicTypes.put(Byte.TYPE, BYTE);
        boxingTypes.put(Byte.class, BYTE);

        //C = char
        basicTypes.put(Character.TYPE, CHAR);
        boxingTypes.put(Character.class, CHAR);

        //D = double
        basicTypes.put(Double.TYPE, DOUBLE);
        boxingTypes.put(Double.class, DOUBLE);

        //F = float
        basicTypes.put(Float.TYPE, FLOAT);
        boxingTypes.put(Float.class, FLOAT);

        //I = int
        basicTypes.put(Integer.TYPE, INT);
        boxingTypes.put(Integer.class, INT);

        //J = long
        basicTypes.put(Long.TYPE, LONG);
        boxingTypes.put(Long.class, LONG);

        //S = short
        basicTypes.put(Short.TYPE, SHORT);
        boxingTypes.put(Short.class, SHORT);

        //Z = boolean
        basicTypes.put(Boolean.TYPE, BOOLEAN);
        boxingTypes.put(Boolean.class, BOOLEAN);

        boxingTypes.forEach((clazz, character) -> reverseBoxingTypes.put(character, clazz));
    }

    public static String getBytecodeTypename(Class<?> clazz) {

        if(clazz.isArray()) {
            return "[" + getBytecodeTypename(clazz.getComponentType());
        }

        Character name = basicTypes.get(clazz);
        if (name == null) {
            name = boxingTypes.get(clazz);
        }
        if (name == null) {
            return OBJECT + clazz.getName().replace('.', '/') + ";";
        }
        return "" + name;
    }

    public static Class<?> forBytecodeTypename(String typename) {
        Class<?> clazz = null;

        if (typename == null || typename.isEmpty()) {
            throw new IllegalArgumentException("Empty type name");
        }

        if (typename.length() == 1) {

            char c = typename.charAt(0);
            if(c == '[') {
                Class<?> componentType = forBytecodeTypename(typename.substring(1));
                return Array.newInstance(componentType, 0).getClass();
            }

            clazz = reverseBoxingTypes.get(c);
        }

        if (typename.startsWith("L") && typename.endsWith(";")) {
            typename = typename.substring(1, typename.length() - 1).replace('/', '.');
            try {
                clazz = Class.forName(typename);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException(e);
            }
        }

        if (clazz == null) {
            throw new IllegalArgumentException("Illegal type name : " + typename);
        } else {
            return clazz;
        }
    }
}
