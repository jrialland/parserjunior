package net.jr.parser.ast;

import net.jr.common.Symbol;
import net.jr.parser.Forward;
import net.jr.parser.ast.annotations.AfterEachNode;
import net.jr.parser.ast.annotations.BeforeEachNode;
import net.jr.parser.ast.annotations.Target;

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

    private static Consumer<AstNode> makeConsumer(Method method, Object visitor) {
        return astNode -> {
            try {
                method.setAccessible(true);
                method.invoke(visitor, astNode);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    private static class Mapping {
        Map<String, List<Consumer<AstNode>>> map = new HashMap<>();
        List<Consumer<AstNode>> befores = new ArrayList<>();
        List<Consumer<AstNode>> afters = new ArrayList<>();
    }

    private static Mapping createMapping(Object visitor) {

        Mapping mapping = new Mapping();

        for (Method method : visitor.getClass().getMethods()) {

            if (method.getAnnotation(BeforeEachNode.class) != null) {
                mapping.befores.add(makeConsumer(method, visitor));
            }

            if (method.getAnnotation(AfterEachNode.class) != null) {
                mapping.afters.add(makeConsumer(method, visitor));
            }

            Target target = method.getAnnotation(Target.class);
            if (target != null) {
                List<Consumer<AstNode>> consumers = mapping.map.computeIfAbsent(target.value(), s -> new ArrayList<>());
                consumers.add(makeConsumer(method, visitor));
            }
        }

        return mapping;
    }

    private static void visitWithMapping(AstNode node, Mapping mapping) {
        for (AstNode child : node.getChildren()) {
            visitWithMapping(child, mapping);
        }
        String symbolName = getSymbolName(node.getSymbol());

        //run 'beforeEachNode' methods
        mapping.befores.forEach(c -> c.accept(node));

        //run the consumers for this symbol name
        List<Consumer<AstNode>> set = mapping.map.get(symbolName);
        if (set != null) {
            set.forEach(c -> c.accept(node));
        }

        // run 'afterEachNode' methods
        mapping.afters.forEach(c -> c.accept(node));
    }

    public static void visit(AstNode rootNode, Object visitor) {
        visitWithMapping(rootNode, createMapping(visitor));
    }


}
