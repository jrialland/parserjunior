package net.jr.parser.ast;

import net.jr.lexer.Token;
import net.jr.parser.Rule;

import java.util.ArrayList;
import java.util.List;

public class AstNodeNonLeaf implements AstNode {

    private Rule rule;

    private List<AstNode> children = new ArrayList<>();

    public AstNodeNonLeaf(Rule rule) {
        this.rule = rule;
    }

    public List<AstNode> getChildren() {
        return children;
    }

    @Override
    public Token asToken() {
        if (children.size() == 1) {
            return children.get(0).asToken();
        }
        return null;
    }
}
