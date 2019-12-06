import { Token } from '../common/Token';
import { ParseSymbol } from '../common/ParseSymbol';

export abstract class AstNode {

    abstract asToken():Token;

    abstract getFirstChild():AstNode;

    abstract getLastChild():AstNode;

    abstract getChildrenOfType(type:ParseSymbol):Array<AstNode>;

    abstract getChildren():Array<AstNode>;

    abstract setChildren(children:Array<AstNode>):void;
};