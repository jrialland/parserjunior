package net.jr.parser.ast;

import net.jr.common.Symbol;
import net.jr.lexer.Token;
import net.jr.parser.Rule;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface AstNode {

    Symbol getSymbol();

    List<AstNode> getChildren();

    Token asToken();

    default List<AstNode> getChildrenOfType(Symbol s) {
        return getChildren().stream().filter(n -> n.getSymbol().equals(s)).collect(Collectors.toList());
    }

    default AstNode getChildOfType(Symbol s) {
        Optional<AstNode> opt = getChildren().stream().filter(n -> n.getSymbol().equals(s)).findFirst();
        return opt.isPresent() ? opt.get() : null;
    }

    default String repr() {
        return String.join(" ", getChildren().stream().map(node -> node.repr()).collect(Collectors.toList()));
    }
}
