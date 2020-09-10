import { Grammar } from '../parser/Grammar';
import { NonTerminal } from '../common/NonTerminal';
import { SingleChar } from './SingleChar';
import { CCharacter } from './CCharacter';
import { Literal } from './Literal';
import { QuotedString } from './QuotedString';
import { Rule } from '../parser/Rule';
import { State, Automaton, AutomatonBuilder, Transition } from './automaton/Automaton';
import { Visitor } from '../parser/Visitor';
import { Terminal } from '../common/Terminal';
import { AstNode } from '../parser/AstNode';
import { CharConstraint } from './CharConstraint';
import { ActionTable } from '../parser/ActionTable';
import { Parser } from '../parser/Parser';


function addTransition(s1: State, constraint: CharConstraint, s2: State) {
	let t = new Transition();
	t.source = s1;
	t.constraint = constraint;
	t.target = s2;
	s1.outgoing.push(t);
	s2.incoming.push(t);
}

function rerouteTransitions(fromState: State, toState: State) {
	for (let outgoing of fromState.outgoing) {
		outgoing.source = toState;
		toState.outgoing.push(outgoing);
		if (outgoing.target == fromState) {
			outgoing.target = toState;
		}
	}
	for (let incoming of fromState.incoming) {
		incoming.target = toState;
		toState.incoming.push(incoming);
		if (incoming.source == fromState) {
			incoming.source = toState;
		}
	}
	fromState.incoming = [];
	fromState.outgoing = [];
}

class RegexGrammar extends Grammar {

	// Terminals
	LeftBrace = new SingleChar('(');
	RightBrace = new SingleChar(')');
	Dot = new SingleChar('.');
	Plus = new SingleChar('+');
	Pipe = new SingleChar('|');
	QuestionMark = new SingleChar('?');
	Star = new SingleChar('*');
	RangeSymbol = new Literal("~");
	SingleQuotedString = new QuotedString('"', '"', '\\', "\n\r");

	// Rules
	Regex: Rule;
	OneOrMoreExpr: Rule;
	Group: Rule;
	CharSequence: Rule;
	Range: Rule;
	Char: Rule;
	AnyChar: Rule;
	Optional: Rule;
	ZeroOrMore: Rule;
	OneOrMore: Rule;
	Or: Rule;

	constructor() {
		super();

		// define some intermediate symbols
		let regex = new NonTerminal("regex")
		let oneOrMoreExpr = new NonTerminal("oneOrMoreExpr");
		let expr = new NonTerminal("expr");

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
		this.Range = this.defineRule(expr, [CCharacter, this.RangeSymbol, CCharacter]).withName("Range");

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
	start: State;
	end: State;
	constructor(start: State, end: State) {
		this.start = start;
		this.end = end;
	}
};

export class RegexVisitor extends Visitor {

	private stack: Array<RegexVisitorContext> = [];

	private stackSize: Array<number> = [];

	private pendingOpt: RegexVisitorContext = null;

	target: Terminal;

	constructor(lexeme: Terminal) {
		super();
		this.target = lexeme;
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
		this.addListener('after', regexGrammar.ZeroOrMore, this.afterZeroOrMore.bind(this));
		this.addListener('after', regexGrammar.Or, this.afterOr.bind(this));
	}

	getAutomaton(): Automaton {
		let context = this.stack.pop();
		context.end.terminal = this.target;
		return new Automaton(context.start);
	}

	private beforeRegex(node: AstNode) {
		this.beforeGroup(node);
	}

	private afterRegex(node: AstNode) {
		this.afterGroup(node);
	}

	private beforeGroup(node: AstNode) {
		this.stackSize.push(this.stack.length);
	}

	private afterGroup(node: AstNode) {
		let items = this.stack.length - this.stackSize.pop();
		while (items > 1) {
			let last = this.stack.pop();
			let nis = this.stack.pop();
			rerouteTransitions(nis.end, last.start);
			this.stack.push(new RegexVisitorContext(nis.start, last.end));
			items -= 1;
		}
	}

	private afterChar(node: AstNode) {
		let c = node.asToken().text[1]; // FIXME handle more complex char specs (escaped etc);
		let start = new State();
		let end = new State();
		addTransition(start, CharConstraint.eq(c), end);
		this.pushContext(start, end);
	}

	private afterCharSequence(node: AstNode) {
		let sequence = node.asToken().text; // FIXME handle more complex string specs (unescape the string)
		if (sequence.length == 0) {
			return;
		}
		sequence = sequence.substring(1, sequence.length - 1);
		let start: State = new State();
		let current: State = start;
		for (let c of sequence) {
			let n: State = new State();
			addTransition(current, CharConstraint.eq(c), n);
			current = n;
		}
		this.pushContext(start, current);
	}

	private afterRange(node: AstNode) {
		let low = node.children[0].asToken().text[1]; //FIXME parse CCharacter properly
		let up = node.children[node.children.length - 1].asToken().text[1]; //FIXME parse CCharacter properly
		let start = new State();
		let end = new State();
		addTransition(start, CharConstraint.inRange(low, up), end);
		this.pushContext(start, end);
	}

	private afterAnyChar(node: AstNode) {
		let start = new State();
		let end = new State();
		addTransition(start, CharConstraint.any(), end);
		this.pushContext(start, end);
	}

	private afterOptional(node: AstNode) {
		let context = this.stack[this.stack.length - 1];
		this.pendingOpt = context;
	}

	private afterZeroOrMore(node: AstNode) {
		this.afterOneOrMore(node);
		this.afterOptional(node);
	}

	private afterOneOrMore(node: AstNode) {
		let context = this.stack[this.stack.length - 1];
		for (let transition of context.start.outgoing) {
			addTransition(context.end, transition.constraint, transition.target);
		}
	}

	private afterOr(node: AstNode) {
		let right = this.stack.pop();
		let left = this.stack.pop();
		//will share the same initial state
		let newInitial = new State();

		rerouteTransitions(left.start, newInitial);
		rerouteTransitions(right.start, newInitial);
		//will share the same final state
		let newFinal = new State();
		rerouteTransitions(left.end, newFinal);
		rerouteTransitions(right.end, newFinal);
		this.pushContext(newInitial, newFinal);
	}

	private pushContext(start: State, end: State) {
		if (this.pendingOpt) {
			for (let transition of start.outgoing) {
				addTransition(this.pendingOpt.start, transition.constraint, transition.target);
			}
			this.pendingOpt = null;
		}
		this.stack.push(new RegexVisitorContext(start, end));
	}
};

export let regexParser = new Parser(new ActionTable(new RegexGrammar));

export class RegexTerminal extends Terminal {

	private _expression: string;

	private _automaton: Automaton;

	constructor(name: string, expression: string) {
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