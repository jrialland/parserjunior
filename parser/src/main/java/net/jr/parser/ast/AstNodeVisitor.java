package net.jr.parser.ast;

import java.util.ArrayList;
import java.util.List;

public class AstNodeVisitor<T> {

    public T visit(AstNode node) {
        List<AstNode> childrenCopy = new ArrayList<>(node.getChildren());
        for (AstNode child : childrenCopy) {
            visit(child);
        }
        return node.getVisited(this);
    }

}
