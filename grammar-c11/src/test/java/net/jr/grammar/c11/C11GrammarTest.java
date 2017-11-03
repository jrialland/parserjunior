package net.jr.grammar.c11;

import net.jr.parser.Forward;
import net.jr.parser.impl.ActionTable;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class C11GrammarTest {

    @BeforeClass
    public static void setupClass() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
    }

    @Test
    public void testPrintln() {
        C11Grammar grammar = new C11Grammar();
        List<String> lines = new ArrayList<>(Arrays.asList(grammar.toString().split("\n")));
        lines = lines.stream().map(l -> l.replaceFirst("\\([0-9]+\\)", "")).collect(Collectors.toList());
        Collections.sort(lines);
        lines = lines.subList(0, lines.size() - 2);
        int i = 1;
        for (String line : lines) {
            System.out.println(Integer.toString(i++) + ".  \t" + line);
        }
    }

    @Test
    public void testConstant() {
        C11Grammar grammar = new C11Grammar();
        ActionTable table = grammar.getActionTable(C11Grammar.Constant);
        System.out.println(table);
    }


    @Test
    public void testConstantExpression() {
        C11Grammar grammar = new C11Grammar();
        ActionTable table = grammar.getActionTable(C11Grammar.ConstantExpression);
        System.out.println(table);
    }

    @Test
    public void testUnaryExpression() {
        C11Grammar grammar = new C11Grammar();
        ActionTable table = grammar.getActionTable(C11Grammar.UnaryExpression);
        System.out.println(table);
    }

    @Test
    public void testPrimaryExpression() {
        C11Grammar grammar = new C11Grammar();
        ActionTable table = grammar.getActionTable(C11Grammar.PrimaryExpression);
        System.out.println(table);
    }



    @Test
    public void testRuleByRule() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "off");

        C11Grammar grammar = new C11Grammar();

        List<Field> fields = new ArrayList<>();

        for (Field f : C11Grammar.class.getFields()) {
            if (Modifier.isStatic(f.getModifiers()) && f.getType().equals(Forward.class)) {
                fields.add(f);
            }
        }

        Collections.sort(fields, Comparator.comparing(Field::getName));

        int total=0, okCount=0;

        for (Field f : fields) {
            total++;
            boolean ok = false;
            try {
                Forward forward = (Forward) f.get(null);
                ActionTable table = grammar.getActionTable(forward);
                ok = true;
                okCount++;
            } catch (Exception e) {
                //no-op
            }
            if (ok) {
                System.out.println("OK " + f.getName());
            } else {
                System.out.println("KO " + f.getName());
            }
        }

        System.out.println("Total : " + total);
        System.out.println("Ok : " + okCount);
    }
}


