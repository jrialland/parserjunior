package net.jr.parser.ast;

import net.jr.common.Symbol;
import net.jr.parser.NonTerminal;
import net.jr.parser.Rule;
import net.jr.parser.ast.annotations.After;
import net.jr.parser.ast.annotations.AfterEachNode;
import net.jr.parser.ast.annotations.Before;
import net.jr.parser.ast.annotations.BeforeEachNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class VisitorHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(VisitorHelper.class);

    private static final Logger getLog() {
        return LOGGER;
    }

    private static String getNameForSymbol(Symbol symbol) {
        if (symbol instanceof NonTerminal) {
            String name = ((NonTerminal) symbol).getName();
            if (name != null) {
                return name;
            }
        }
        return symbol.getClass().getSimpleName();
    }

    private static String getName(AstNode node) {
        Rule rule = node.getRule();
        if (rule != null) {
            String ruleName = rule.getName();
            return ruleName == null ? getNameForSymbol(rule.getTarget()) : ruleName;
        } else {
            return getNameForSymbol(node.getSymbol());
        }
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

    private static Mapping createMapping(Object visitor) {

        Mapping mapping = new Mapping();

        for (Method method : visitor.getClass().getMethods()) {

            if (method.getAnnotation(BeforeEachNode.class) != null) {
                mapping.beforeEachNode.add(makeConsumer(method, visitor));
            }

            if (method.getAnnotation(AfterEachNode.class) != null) {
                mapping.afterEachNode.add(makeConsumer(method, visitor));
            }

            Before before = method.getAnnotation(Before.class);
            if (before != null) {
                List<Consumer<AstNode>> consumers = mapping.mapBefore.computeIfAbsent(before.value(), s -> new ArrayList<>());
                consumers.add(makeConsumer(method, visitor));
            }

            After after = method.getAnnotation(After.class);
            if (after != null) {
                List<Consumer<AstNode>> consumers = mapping.mapAfter.computeIfAbsent(after.value(), s -> new ArrayList<>());
                consumers.add(makeConsumer(method, visitor));
            }
        }

        return mapping;
    }

    private static void _trace(AstNode node, String name, String phase) {
        if (getLog().isTraceEnabled()) {
            String nodeToString = node.toString();
            if (nodeToString.equals(name)) {
                getLog().trace(phase + " " + nodeToString);
            } else {
                getLog().trace(String.format("%s %s \t %s", phase, name, node.toString()));
            }
        }
    }

    private static void visitWithMapping(AstNode node, Mapping mapping) {

        String name = getName(node);

        _trace(node, name, "Before");

        //run 'beforeEachNode' consumers
        mapping.beforeEachNode.forEach(c -> c.accept(node));

        //run @@Before consumers for this symbol name
        List<Consumer<AstNode>> before = mapping.mapBefore.get(name);
        if (before != null) {
            before.forEach(c -> c.accept(node));
        }

        //visit children
        for (AstNode child : node.getChildren()) {
            visitWithMapping(child, mapping);
        }

        _trace(node, name, "After");

        //run @After consumers for this symbol name
        List<Consumer<AstNode>> set = mapping.mapAfter.get(name);
        if (set != null) {
            set.forEach(c -> c.accept(node));
        }

        // run 'afterEachNode' consumers
        mapping.afterEachNode.forEach(c -> c.accept(node));
    }

    public static void visit(AstNode rootNode, Object visitor) {
        visitWithMapping(rootNode, createMapping(visitor));
    }

    private static class Mapping {
        Map<String, List<Consumer<AstNode>>> mapBefore = new HashMap<>();
        Map<String, List<Consumer<AstNode>>> mapAfter = new HashMap<>();
        List<Consumer<AstNode>> beforeEachNode = new ArrayList<>();
        List<Consumer<AstNode>> afterEachNode = new ArrayList<>();
    }


}
