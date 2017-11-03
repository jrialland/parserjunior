package net.jr.parser.ast;

import net.jr.lexer.Token;

import java.util.List;

public interface AstNode {

    List<AstNode> getChildren();

    Token asToken();
}
