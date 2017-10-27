package net.jr.parser.ast;

import net.jr.lexer.Token;

import java.util.Collections;
import java.util.List;

public class AstNodeLeaf implements AstNode {

    private Token token;

    public AstNodeLeaf(Token token) {
        this.token = token;
    }

    @Override
    public List<AstNode> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public Token asToken() {
        return token;
    }

    @Override
    public String toString() {
        return token.toString();
    }
}
