package net.jr.lexer.expr;

import net.jr.lexer.impl.CharConstraint;

public class Transition {

    private CharConstraint charConstraint;

    private Node source;

    private Node target;

    public Transition(Node source) {
        super();
        this.source = source;
    }

    public void setCharConstraint(CharConstraint charConstraint) {
        this.charConstraint = charConstraint;
    }

    public CharConstraint getCharConstraint() {
        return charConstraint;
    }

    public Node toNewNode() {
        Node node = new Node();
        toNode(node);
        return node;
    }

    public void toNode(Node n) {
        this.target = n;
        n.getIncomingTransitions().add(this);
    }

    public Node getSource() {
        return source;
    }

    public Node getTarget() {
        return target;
    }

    public void setTarget(Node target) {
        if(target != null) {
            target.getIncomingTransitions().remove(this);
        }
        this.target = target;
    }
}
