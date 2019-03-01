package net.jr.parser.ast;

import net.jr.common.Symbol;
import net.jr.lexer.Token;
import net.jr.parser.Rule;

import java.util.*;
import java.util.stream.Collectors;

public interface AstNode {

    Rule getRule();

    Symbol getSymbol();

    List<AstNode> getChildren();

    default AstNode getFirstChild() {
        List<AstNode> children = getChildren();
        if (children.isEmpty()) {
            return null;
        }
        return children.get(0);
    }

    default AstNode getChildAt(int index) {
        return getChildren().get(index);
    }

    default AstNode getLastChild() {
        List<AstNode> children = getChildren();
        if (children.isEmpty()) {
            return null;
        }
        return children.get(children.size() - 1);
    }

    Token asToken();

    default List<AstNode> getChildrenOfType(Symbol s) {
        return getChildren().stream().filter(n -> n.getSymbol().equals(s)).collect(Collectors.toList());
    }

    default AstNode getChildOfType(Symbol s) {
        Optional<AstNode> opt = getChildren().stream().filter(n -> n.getSymbol().equals(s)).findFirst();
        return opt.isPresent() ? opt.get() : null;
    }

    default List<AstNode> getDescendants() {
        List<AstNode> descendants = new ArrayList<>();
        for (AstNode child : getChildren()) {
            if (child.getChildren().isEmpty()) {
                descendants.add(child);
            } else {
                descendants.addAll(child.getDescendants());
            }
        }
        return descendants;
    }

    default AstNode getDescendantOfType(Symbol s) {
        if (getChildren().isEmpty()) {
            return null;
        }
        AstNode tmp;
        for (AstNode child : getChildren()) {
            if ((tmp = child).getSymbol().equals(s)) {
                return tmp;
            }
            if ((tmp = child.getDescendantOfType(s)).getSymbol().equals(s)) {
                return tmp;
            }
        }
        return null;
    }

    default List<AstNode> getDescendantsOfType(Symbol s) {
        List<AstNode> list = new ArrayList<>();
        AstNode tmp;
        for (AstNode child : getChildren()) {
            if ((tmp = child).getSymbol().equals(s)) {
                list.add(tmp);
            }
            list.addAll(child.getDescendantsOfType(s));
        }
        return list;
    }

    default String repr() {
        return String.join(" ", getChildren().stream().map(node -> node.repr()).collect(Collectors.toList()));
    }
}
