package net.jr.parser.ast;

import java.util.ArrayList;
import java.util.List;

public class AstNodeVisitor<T> {

    public List<T> visitChildren(AstNode node) {
        List<T> list = new ArrayList<>();
        for (AstNode child : node.getChildren()) {
            list.add(visit(child));
        }
        return list;
    }

    public T visit(AstNode node) {
        visitChildren(node);
        return null;
    }

}
