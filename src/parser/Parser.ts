import { AstNode } from "./AstNode"
import { LexerStream } from "../lexer/LexerStream";
import { Token } from "../common/Token";
import { ActionTable, Action, ActionType } from "./ActionTable";
import { Empty, Eof } from "../common/SpecialTerminal";
import { Rule } from "./Rule";
import { Terminal } from "../common/Terminal";
import { ParseSymbol } from "../common/ParseSymbol";
import { Reader } from "../common/Reader";


export class ParseError extends Error {
    constructor(token:Token, expected:Array<Terminal>) {
        super("Parse error");
    }
};

export interface ParserListener {
    onParseError(error:ParseError, parser:Parser, lexerStream:LexerStream, node:AstNode):void;
    onReduce(parser:Parser, lexerStream:LexerStream, node:AstNode):void;
};

class LeafNode extends AstNode {

    private _rule:Rule;

    getChildren(): AstNode[] {
        return [];
    }

    setChildren(children: AstNode[]): void {
        throw new Error("Method not implemented.");
    }

    private _token:Token;

    constructor(token:Token, rule:Rule) {
        super();
        this._rule = rule;
        this._token = token;
    }

    get rule() {
        return this._rule;
    }

    asToken():Token {
        return this._token;
    }

    getChildrenOfType(type:ParseSymbol):Array<AstNode> {
        return new Array<AstNode>(0);
    }

    getFirstChild():AstNode{
        return null;
    }

    getLastChild(): AstNode {
        return null;
    }
};

class NonLeafNode extends AstNode {

    getLastChild(): AstNode {
        if(this._children.length == 0) {
            return null;
        }
        return this._children[this._children.length-1];
    }

    getChildren(): AstNode[] {
        return this._children;
    }

    setChildren(children: AstNode[]): void {
        this._children = children;
    }

    private _rule:Rule;

    private _children:AstNode[];

    constructor(rule:Rule, children:AstNode[]) {
        super();
        this._rule = rule;
        this._children = children;
    }

    asToken():Token {
        if(this._children.length == 0) {
            return this._children[0].asToken();
        }
        return null;
    }

    getFirstChild():AstNode {
        if(this._children.length > 0) {
            return this._children[0];
        } else {
            return null;
        }
    }

    getChildrenOfType(type:ParseSymbol):Array<AstNode> {
        return this._children.filter(node => node.asToken().tokenType == type);
    }

    get rule() {
        return this._rule;
    }

};

class Context {

    node:AstNode;

    state:number;

    constructor(node:AstNode, state:number) {
        this.node = node;
        this.state = state;
    }
};

export class Parser {

    private actionTable:ActionTable;
    
    lexerStream:LexerStream;

    parserListener:ParserListener;
    
    constructor(actionTable:ActionTable) {
        this.actionTable = actionTable;
    }

    setLexerStream(lexerStream:LexerStream) {
        this.lexerStream = lexerStream;
    }

    private makeLexerStream(reader:Reader):LexerStream {
        if(this.lexerStream != null) {
            return this.lexerStream;
        } else {
            let terminals = this.actionTable.grammar.getTerminals();
            return new LexerStream(reader, terminals as Terminal[], []);
        }
    }

    private makeNode(stack:Array<Context>, lexerStream:LexerStream, rule:Rule):AstNode {
        let children = [];
        for(let i=0; i < rule.definition.length; i++) {
            let popped = stack.pop().node;
            if(popped.asToken().tokenType != Eof) {
                children.push(popped);
            }
        }
        children.reverse();
        let node = new NonLeafNode(rule, children);
        let reduceAction:(parser:Parser, lexerStream:LexerStream, node:AstNode)=>void = rule.getReduceAction();
        if(reduceAction != null) {
            reduceAction(this, lexerStream, node);
        }
        if(this.parserListener != null) {
            this.parserListener.onReduce(this, lexerStream, node);
        }
        return node;
    }

    private accept(stack:Array<Context>, lexerStream:LexerStream) {
        let targetRule = this.actionTable.grammar.targetRule;
        let node = this.makeNode(stack, lexerStream, targetRule);
        stack.push(new Context(node, 0));
    }

    private fail(stack:Array<Context>, token:Token, lexerStream:LexerStream, context:Context) {
        let parseError = new ParseError(token, this.actionTable.getExpectedTerminals(context.state));
        if (this.parserListener != null) {
            this.parserListener.onParseError(parseError, this, lexerStream, context.node);
        } else {
            throw parseError;
        }
    }

    private shift(stack:Array<Context>,token:Token, nextState:number) {
        let rule = this.actionTable.grammar.getRuleById(nextState);
        stack.push(new Context(new LeafNode(token, rule), nextState));
    }

    private reduce(stack:Array<Context>, lexerStream:LexerStream, ruleIndex:number) {
        let rule = this.actionTable.grammar.getRuleById(ruleIndex)
        let node = this.makeNode(stack, lexerStream, rule);
        let newState = this.actionTable.getNextState(stack[stack.length-1].state, rule.target);
        stack.push(new Context(node, newState));
    }

    parse(reader:Reader):AstNode {
        
        let lexerStream:LexerStream = this.makeLexerStream(reader);
        let targetRule:Rule = this.actionTable.grammar.targetRule;
        let stack:Array<Context> = [new Context(new NonLeafNode(targetRule, []), 0)];

        while(true) {

            let currentContext:Context = stack[stack.length-1];
            let currentState:number = currentContext.state;
            let token:Token = lexerStream.next().value;
            let decision:Action = this.actionTable.getAction(currentState, token.tokenType);

            if(decision == null) {
                let expected = this.actionTable.getExpectedTerminals(currentState);
                if(expected.includes(Empty)) {
                    decision = this.actionTable.getAction(currentState, Empty);
                    lexerStream.pushback(token);
                } else {
                    decision = new Action(ActionType.Fail, 0);
                }
            }

            switch(decision.type) {
                case ActionType.Accept:
                    this.accept(stack, lexerStream);
                    return stack.pop().node;
                case ActionType.Fail:
                    this.fail(stack, token, lexerStream, currentContext);
                    break;
                case ActionType.Shift:
                    this.shift(stack, token, decision.target);
                    break;
                case ActionType.Reduce:
                    this.reduce(stack, lexerStream, decision.target);
                    lexerStream.pushback(token);
                    break;
                default:
                    throw new Error("Illegal action type '"+decision.type.toString()+"' !");
            }

        }
    }
}