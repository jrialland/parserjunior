import { Token } from "../common/Token";
import { Empty, Eof } from "../common/SpecialTerminal";
import { Terminal } from "../common/Terminal";
import { Reader } from "../common/Reader";

import { SingleChar } from "../lexer/SingleChar";
import { LexerStream } from "../lexer/LexerStream";

import { Rule } from "./Rule";
import { AstNode } from "./AstNode"
import { ActionTable, Action, ActionType } from "./ActionTable";

import jsesc = require('jsesc');
// -----------------------------------------------------------------------------
export class ParseError extends Error {
    expected:Array<Terminal>;
    constructor(token:Token, expected:Array<Terminal>) {
        super("Parse error");
        this.expected = expected;
    }
};

// -----------------------------------------------------------------------------
export interface ParserListener {
    onParseError(error:ParseError, parser:Parser, lexerStream:LexerStream, node:AstNode):void;
    onReduce(parser:Parser, lexerStream:LexerStream, node:AstNode):void;
};

// -----------------------------------------------------------------------------
class ParserState {
    stateId:number;
    node:AstNode;
    constructor(stateId:number, node:AstNode) {
        this.stateId = stateId;
        this.node = node;
    }
};

// -----------------------------------------------------------------------------
class LeafNode implements AstNode {

    token:Token;
    
    constructor(token:Token) {
        this.token = token;
    }

    get rule():Rule {
        return null;
    }

    asToken() {
        return this.token;
    }

    set children(a:Array<AstNode>) {
    }

    get children():Array<AstNode> {
        return [];
    }
};

// -----------------------------------------------------------------------------
class NonLeafNode implements AstNode {
    rule:Rule;
    children:Array<AstNode>;
    constructor(rule:Rule, children:Array<AstNode>) {
        this.rule = rule;
        this.children = children;
    }

    asToken():Token {
        if(this.children.length > 0) {
            return this.children[0].asToken();
        } else {
            return null;
        }
    }
};

// -----------------------------------------------------------------------------
export class Parser {

    private actionTable:ActionTable;
    
    parserListener:ParserListener;
    
    constructor(actionTable:ActionTable) {
        this.actionTable = actionTable;
    }

    parseString(s:string, ignore?:Array<Terminal>):AstNode {
        return this.parseReader(Reader.fromString(s), ignore);
    }

    parseReader(reader:Reader, ignore?:Array<Terminal>):AstNode {
        let terminals:Array<Terminal> = this.actionTable.grammar.getTerminals() as Array<Terminal>;
        if(!ignore) {
            ignore = [
                new SingleChar(' '),
                new SingleChar('\t'),
                new SingleChar('\r'),
                new SingleChar('\n'),
            ];
        }
        let lexerStream = new LexerStream(reader, terminals, ignore);
        return this.parse(lexerStream);
    }
    
    parse(stream:LexerStream) {
        
        let stack = [new ParserState(0, null)];

        while(true) {

            // Top of the stack
            let currentState = stack[stack.length-1];

            let next = stream.next();

            if(next.done) {
                // We should never reach the end of the lexer stream, an 'Accept' action should arise on the last token, making this method return
                // -> Getting past the last token is an error (i.e the last character is missing etc... )
                let lastNode = stack[stack.length-1].node;
                let expected = this.actionTable.getExpectedTerminals(stack[stack.length-1].stateId);
                throw new ParseError(lastNode.asToken(), expected);

            } else {

                // Incoming token
                let token = next.value as Token;

                // Find the action to be taken
                let action = this.actionTable.getAction(currentState.stateId, token.tokenType);
                if(action == null) {
                    //special case when 'empty' is acceptable
                    action = this.actionTable.getAction(currentState.stateId, Empty);
                    if(action != null) {
                        // The token is not consumed in this case
                        stream.pushback(token);
                    }
                }

                // The input is not illegal for this State 
                if(action == null) {
                    throw new ParseError(token, this.actionTable.getExpectedTerminals(currentState.stateId));
                }

                switch(action.type) {
                    case ActionType.Shift:
                        this.shift(stack, token, action);
                        break;
                    case ActionType.Reduce:
                        this.reduce(stack, token, action, stream);
                        break;
                    case ActionType.Accept:
                            let targetRule = this.actionTable.grammar.getTargetRule();
                            return this.makeNode(stack, stream, targetRule);
                    case ActionType.Fail:
                        this.fail(token, stream, currentState);
                        break;
                }
            }
        }
    }

    private makeNode(stack:Array<ParserState>, stream:LexerStream, rule:Rule):AstNode {
        let children:Array<AstNode> = [];
        for(let sym of rule.definition) {
            let popped = stack.pop().node;
            let poppedToken = popped.asToken();
            let isEof = poppedToken != null && poppedToken.tokenType === Eof;
            if( ! isEof) {
                children.push(popped);
            }
        }
        return new NonLeafNode(rule, children.reverse());
    }

    private shift(stack:Array<ParserState>, token:Token, action:Action) {
        stack.push(new ParserState(action.target, new LeafNode(token)));
    }

    private reduce(stack:Array<ParserState>, token:Token, action:Action, stream:LexerStream) {
        let rule = this.actionTable.grammar.getRuleById(action.target);
        let node = this.makeNode(stack, stream, rule);
        let stateId = stack[stack.length-1].stateId;
        // A new state is searched in the GOTO table and becomes the current state
        let nextStateId = this.actionTable.getNextState(stateId, rule.target);
        stack.push(new ParserState(nextStateId, node));
        stream.pushback(token);
    }

    private fail(token:Token, stream:LexerStream, state:ParserState) {
        let parseError = new ParseError(token, this.actionTable.getExpectedTerminals(state.stateId));
        if(this.parserListener) {
            this.parserListener.onParseError(parseError, this, stream, state.node);
        } else {
            throw parseError;
        }
    }
};