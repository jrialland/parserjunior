package net.jr.grammar.c11;

import net.jr.common.Symbol;
import net.jr.lexer.Lexeme;
import net.jr.parser.Forward;
import net.jr.parser.ast.AstNode;

import java.lang.reflect.Method;

public class CGrammarHelper {

    private static String getSymbolName(Symbol symbol) {
        if (symbol instanceof Lexeme) {
            return "Lexeme";
        }
        if (symbol instanceof Forward) {
            return ((Forward) symbol).getName();
        }
        return null;
    }

    private static Method getVisitorMethod(Class<?> clazz, String name) {
        String methodName = "visit" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
        try {
            return clazz.getMethod(methodName, AstNode.class);
        } catch (Exception e) {
            return null;
        }
    }

    public static void visit(AstNode root, Object visitor) {
        for (AstNode child : root.getChildren()) {
            visit(child, visitor);
        }
        String name = getSymbolName(root.getSymbol());
        Method method = name == null ? null : getVisitorMethod(visitor.getClass(), name);
        if (method != null) {
            try {
                method.invoke(visitor, root);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}
