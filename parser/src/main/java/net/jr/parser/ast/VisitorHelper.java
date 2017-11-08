package net.jr.parser.ast;

import net.jr.common.Symbol;
import net.jr.parser.Forward;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class VisitorHelper {

    private static String getSymbolName(Symbol symbol) {

        if (symbol instanceof Forward) {
            String name = ((Forward) symbol).getName();
            if (name != null) {
                return name;
            }
        }

        return symbol.getClass().getSimpleName();
    }

    private static Map<String, List<Consumer<AstNode>>> createMapping(Object visitor) {
        Map<String, List<Consumer<AstNode>>> map = new HashMap<>();
        for (Method method : visitor.getClass().getMethods()) {
            Target target = method.getAnnotation(Target.class);
            if (target != null) {
                List<Consumer<AstNode>> consumers = map.computeIfAbsent(target.value(), s -> new ArrayList<>());
                consumers.add(astNode -> {
                    try {
                        method.setAccessible(true);
                        method.invoke(visitor, astNode);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
        return map;
    }

    private static void visitWithConsumers(AstNode node, Map<String, List<Consumer<AstNode>>> consumers) {
        for (AstNode child : node.getChildren()) {
            visitWithConsumers(child, consumers);
        }
        String symbolName = getSymbolName(node.getSymbol());
        List<Consumer<AstNode>> set = consumers.get(symbolName);
        if (set != null) {
            set.forEach(c -> c.accept(node));
        }
    }

    public static void visit(AstNode rootNode, Object visitor) {
        Map<String, List<Consumer<AstNode>>> mapping = createMapping(visitor);
        visitWithConsumers(rootNode, mapping);
    }


}
