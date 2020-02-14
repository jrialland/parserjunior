import {Grammar} from '../parser/Grammar';
import {NonTerminal} from '../common/NonTerminal';
import {SingleChar} from './SingleChar';
import {CCharacter} from './CCharacter';
import {Literal} from './Literal';
import {QuotedString} from './QuotedString';
import { Rule } from '../parser/Rule';
import { State, Automaton, AutomatonBuilder } from './automaton/Automaton';
import { Visitor } from '../parser/Visitor';
import { Terminal } from '../common/Terminal';
import { AstNode } from '../parser/AstNode';
import { CharConstraint } from './CharConstraint';

class RegexGrammar extends Grammar {

	// Terminals
	LeftBrace = new SingleChar('(');
	RightBrace = new SingleChar(')');
	Dot = new SingleChar('.');
	Plus = new SingleChar('+');
	Pipe = new SingleChar('|');
	QuestionMark = new SingleChar('?');
	Star = new SingleChar('*');
	ThreePoints = new Literal("..");
	SingleQuotedString = new QuotedString('\'', '\'', '\\', "\n\r");

	// Rules
	Regex:Rule;
	OneOrMoreExpr:Rule;
	Group:Rule;
	CharSequence:Rule;
	Range:Rule;
	Char:Rule;
	AnyChar:Rule;
	Optional:Rule;
	ZeroOrMore:Rule;
	OneOrMore:Rule;
	Or:Rule;

	constructor() {
		super();

		// define some intermediate symbols
		let regex = new NonTerminal("regex")
		let oneOrMoreExpr = new NonTerminal("oneOrMoreExpr");
		let expr =new NonTerminal("expr");
		
		// OneOrMoreExpr → Expr+
		this.OneOrMoreExpr = this.defineRule(oneOrMoreExpr, [this.oneOrMore(expr)]).withName("OneOrMoreExpr");

		// Regex → OneOrMoreExpr
		this.Regex = this.defineRule(regex, [oneOrMoreExpr]).withName("Regex");
		this.setTargetRule(this.Regex);

		// Expr → '(' OneOrMoreExpr ')'
		this.Group = this.defineRule(expr, [this.LeftBrace, oneOrMoreExpr, this.RightBrace]).withName("Group");

		// Expr → Sequence
		this.CharSequence = this.defineRule(expr, [this.SingleQuotedString]).withName("CharSequence");

		// Expr → CCharacter '...' CCharacter
		this.Range = this.defineRule(expr, [CCharacter, this.ThreePoints, CCharacter]).withName("Range");

		// Expr → CCharacter
		this.Char = this.defineRule(expr, [CCharacter]).withName("Char");
		
		// Expr → '.'
		this.AnyChar = this.defineRule(expr, [this.Dot]).withName("AnyChar");

		// Expr → Expr '?'
		this.Optional = this.defineRule(expr, [expr, this.QuestionMark]).withName("Optional");

		// Expr → Expr '*'
		this.ZeroOrMore = this.defineRule(expr, [expr, this.Star]).withName("ZeroOrMore");
		
		// Expr → Expr '+'
		this.OneOrMore = this.defineRule(expr, [expr, this.Plus]).withName("OneOrMore");
		
		// Expr → Expr '|' Expr
        this.Or = this.defineRule(expr, [expr, this.Pipe, expr]).withName("Or");
	
	}
};

export const regexGrammar = new RegexGrammar;

class RegexVisitorContext {
	start:State;
	end:State;
	constructor(start:State, end:State) {
		this.start = start;
		this.end = end;
	}
};

export class RegexVisitor extends Visitor {

	stack:Array<RegexVisitorContext> = [];

	stackSize:Array<number> = [];
	
	builder:AutomatonBuilder;

	target:Terminal;

	constructor(lexeme:Terminal) {
		super();
		this.target = lexeme;
		this.builder = AutomatonBuilder.forTokenType(lexeme);
		this.addListener('before', regexGrammar.Regex, this.beforeRegex.bind(this));
		this.addListener('after', regexGrammar.Regex, this.afterRegex.bind(this));
		this.addListener('before', regexGrammar.Group, this.beforeGroup.bind(this));
		this.addListener('after', regexGrammar.Group, this.afterGroup.bind(this));
		this.addListener('after', regexGrammar.CharSequence, this.afterCharSequence.bind(this));
		this.addListener('after', regexGrammar.Range, this.afterRange.bind(this));
		this.addListener('after', regexGrammar.Char, this.afterChar.bind(this));
		this.addListener('after', regexGrammar.AnyChar, this.afterAnyChar.bind(this));
		this.addListener('after', regexGrammar.Optional, this.afterOptional.bind(this));
		this.addListener('after', regexGrammar.OneOrMore, this.afterOneOrMore.bind(this));
		this.addListener('after', regexGrammar.Or, this.afterOr.bind(this));
	}
	
	getAutomaton():Automaton {
		let context = this.stack.pop();
		context.end.terminal = this.target;
		return this.builder.build();
	}

	beforeRegex(node:AstNode) {
		this.beforeGroup(node);
	}
	
	afterRegex(node:AstNode) {
		this.afterGroup(node);
	}

	beforeGroup(node:AstNode) {
		this.stackSize.push(this.stack.length);
	}

	afterGroup(node:AstNode) {
		let items = this.stack.length - this.stackSize.pop();
		while(items > 1) {
			let right:RegexVisitorContext = this.stack.pop();
			let left:RegexVisitorContext = this.stack.pop();
			let mergeNode = new State();

			mergeNode.fallbackTransition = right.start.fallbackTransition;
			mergeNode.terminal = right.start.terminal;

			// connections that used to go to first.end now go to mergeNode
			left.end.incoming.forEach( transition => {
				transition.target = mergeNode;
			});
		
			// connections that used to originate from first.end now originate from mergeNode
			left.end.outgoing.forEach(transition => {
				transition.source = mergeNode;
			});

			// connections that used to go ot next.start now go to mergeNode
			right.start.incoming.forEach(transition => {
				transition.target = mergeNode;
			});

			// connections that used originate from next.start now originate from mergeNode
			right.start.outgoing.forEach(transition => {
				transition.source = mergeNode;
			});

			let grouped:RegexVisitorContext = new RegexVisitorContext(left.start, left.end);
			this.stack.push(grouped);

			items -=1;
		}
	}

	afterCharSequence(node:AstNode) {
		/*
		let sequence = node.asToken().text;
		let start:State = new State();
		let current:State = start;
		for(let c of sequence) {
			let n:State = new State();
			current.addTransition(CharConstraint.eq(c)).target = n;
			current = n;
		}
		current.terminal = this.lexeme;
		this.stack.push(new Context(start, current));
		*/
	}
		
	afterRange(node:AstNode) {
		/*
        node = node.children[0];
        let children:Array<AstNode> = node.children.filter(n=>n.asToken().tokenType===RegexGrammar.Char);
        let low:string = children[0].asToken().text;
        let up:string = children[1].asToken().text;
        let start = new State();
        let end = new State();
        start.addTransition(CharConstraint.inRange(low, up)).target = end;
        end.terminal = this.lexeme;
		this.stack.push(new Context(start, end));
		*/
    }

    afterChar(node:AstNode) {
		let c = node.asToken().text[1]; // FIXME : Handle more complex char definitions (escapes, etc)
        let start = this.builder.initialState();
        let end = this.builder.newFinalState();
        start.addTransition(CharConstraint.eq(c)).target = end;
		this.stack.push(new RegexVisitorContext(start, end));
    }

    afterAnyChar(node:AstNode) {
        /*let start = this.builder.initialState();
        let end = this.builder.newFinalState();
        start.addTransition(CharConstraint.any()).target = end;
		this.stack.push(new RegexVisitorContext(start, end));
		*/
    }

    afterOptional(node:AstNode) {
		/*
		let context = this.stack[this.stack.length-1];
		let skip = context.start.addFallback();
		skip.target = context.end;
		*/
    }

    afterZeroOrMore(node:AstNode) {
        let context = this.stack[this.stack.length-1];
		context.start.terminal = this.target; // having nothing is ok
		for(let t of context.end.incoming) {
			t.target = context.start;
		}
		context.end = context.start;
    }

	afterOneOrMore(node:AstNode) {
		/*
        let context = this.stack[this.stack.length-1];
		let n2 = new State();
		for(let t of context.start.outgoing) {
			n2.addTransition(t.constraint).target = t.target;
		}
		for(let t of context.end.incoming) {
			t.target = n2;
		}
		context.end = n2;
		*/
    }

    afterOr(node:AstNode) {
		
        let right = this.stack.pop();
		let left = this.stack.pop();
		
		// create 2 new automaton states
		let firstNode = this.builder.newNonFinalState();
		let lastNode = this.builder.newFinalState();
		
		//for each transition from the left expression
        for (let t of left.start.outgoing) {
			// add a transition for the new graph
            firstNode.addTransition(t.constraint).target = t.target;
		}
		// do the same with the expression on the right
        for (let t of right.start.outgoing) {
            firstNode.addTransition(t.constraint).target = t.target;
		}
		
		// we disconnect the former starting nodes
		left.start.disconnect();
		right.start.disconnect();
		
		// we also de reroute the end transitions
		for(let t of left.end.incoming) {
			t.target = lastNode;
		}
		for(let t of right.end.incoming) {
			t.target = lastNode;
		}

		// we disconnect the former ending nodes
        right.end.disconnect();
		left.end.disconnect();
		
		this.stack.push(new RegexVisitorContext(firstNode, lastNode));
    }
};
/*
let regexParser = new Parser(new ActionTable(new RegexGrammar));

export class RegexTerminal extends Terminal {

    private _expression:string;

    private _automaton:Automaton;

    constructor(name:string, expression:string) {
        super(name);
        this._expression = expression;
        let ast = regexParser.parseString(this._expression);
        let visitor = new RegexVisitor(this);
        visitor.visit(ast);
		this._automaton = visitor.getAutomaton();
    }

    get automaton() {
        return this._automaton;
    }
};
*/