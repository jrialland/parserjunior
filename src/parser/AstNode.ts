import { Token } from '../common/Token';
import { ParseSymbol } from '../common/ParseSymbol';
import { Rule } from './Rule';

export abstract class AstNode {

    abstract get rule():Rule;

    abstract asToken():Token;

    abstract getFirstChild():AstNode;

    abstract getLastChild():AstNode;

    abstract getChildrenOfType(type:ParseSymbol):Array<AstNode>;

    abstract getChildren():Array<AstNode>;

    abstract setChildren(children:Array<AstNode>):void;
};