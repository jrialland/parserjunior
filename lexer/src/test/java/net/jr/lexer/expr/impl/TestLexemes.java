package net.jr.lexer.expr.impl;

import net.jr.lexer.Terminal;
import net.jr.lexer.basicterminals.*;
import net.jr.lexer.impl.OneOf;
import net.jr.marshalling.MarshallingUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class TestLexemes {

    private static Map<Class<?>, Callable<? extends Terminal>> types = new HashMap<>();

    static {
        types.put(Artificial.class, () -> new Artificial("Test"));
        types.put(CBinary.class, () -> new CBinary());
        types.put(CCharacter.class, () -> new CCharacter());
        types.put(CFloatingPoint.class, () -> new CFloatingPoint());
        types.put(CHexNumber.class, () -> new CHexNumber());
        types.put(CInteger.class, () -> new CInteger());
        types.put(COctal.class, () -> new COctal());
        types.put(CString.class, () -> new CString());
        types.put(LineComment.class, () -> new LineComment("--"));
        types.put(Literal.class, () -> new Literal("test"));
        types.put(MultilineComment.class, () -> new MultilineComment("/**", "*/"));
        types.put(NewLine.class, () -> new NewLine());
        types.put(OneOf.class, () -> new OneOf("abcd"));
        types.put(QuotedString.class, () -> new QuotedString('\'', '\'', '\\', new char[]{}));
        types.put(SingleChar.class, () -> new SingleChar('x'));
        types.put(Word.class, () -> new Word("test"));
    }

    @Test
    public void testEquals() {
        types.entrySet().forEach(entry -> {

            try {

                Terminal l = entry.getValue().call();

                Assert.assertFalse(l.equals("hehe"));
                Assert.assertFalse(l.equals(null));
                Assert.assertTrue(l.equals(l));
                Assert.assertTrue(l.equals(entry.getValue().call()));

            } catch (Exception e) {
                throw new RuntimeException(entry.getKey().getName(), e);
            }
        });
    }

    @Test
    public void testHashCode() {
        types.entrySet().forEach(entry -> {
            try {
                Terminal l = entry.getValue().call();
                Terminal l2 = entry.getValue().call();
                Assert.assertEquals(l.hashCode(), l.hashCode());
                Assert.assertEquals(l.hashCode(), l2.hashCode());
            } catch (Exception e) {
                throw new RuntimeException(entry.getKey().getName(), e);
            }
        });
    }

    @Test
    public void testMarshall() {
        types.entrySet().forEach(entry -> {
            try {
                Terminal l = entry.getValue().call();
                byte[] bytes = MarshallingUtil.toByteArray(l, true);
                Assert.assertFalse(bytes.length == 0);
                Terminal l2 = MarshallingUtil.fromByteArray(bytes, true);
                Assert.assertEquals(l, l2);
            } catch (Exception e) {
                throw new RuntimeException(entry.getKey().getName(), e);
            }
        });


    }
}
