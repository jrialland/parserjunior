package net.jr.lexer.expr.impl;

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

    public void setSource(Node source) {
        if(this.source != null) {
            this.source.getOutgoingTransitions().remove(this);
        }
        this.source = source;
        this.source.getOutgoingTransitions().add(this);
    }

    public Node getTarget() {
        return target;
    }

    public void setTarget(Node target) {
        if(this.target != null) {
            this.target.getIncomingTransitions().remove(this);
        }
        this.target = target;
        this.target.getIncomingTransitions().add(this);
    }

    @Override
    public String toString() {
        return charConstraint.toString();
    }
}
