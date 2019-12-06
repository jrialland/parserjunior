import { State, Transition } from "./automaton/Automaton";
import { Visitor } from "../parser/Visitor";
import { Terminal } from "../common/Terminal";
import { AstNode } from "../parser/AstNode";
import { CharConstraint } from "./CharConstraint";
import { RegexGrammar } from "./RegexGrammar";

class Context {
	start:State;
	end:State;
	constructor(start:State, end:State) {
		this.start = start;
		this.end = end;
	}
};

class RegexVisitor extends Visitor {

	stack:Array<Context>;

	stackSize:Array<number>;
	
	lexeme:Terminal;

	constructor(lexeme:Terminal) {
		super();
		this.lexeme = lexeme;
		this.addListener('before', 'Regex', this.beforeRegex.bind(this));
		this.addListener('after', 'Regex', this.afterRegex.bind(this));
		this.addListener('before', 'Group', this.beforeGroup.bind(this));
		this.addListener('after', 'Group', this.afterGroup.bind(this));
		this.addListener('after', 'CharSequence', this.afterCharSequence.bind(this));
		this.addListener('after', 'Range', this.afterRange.bind(this));
		this.addListener('after', 'Char', this.afterChar.bind(this));
		this.addListener('after', 'AnyChar', this.afterAnyChar.bind(this));
		this.addListener('after', 'Optional', this.afterOptional.bind(this));
		this.addListener('after', 'OneOrMore', this.afterOneOrMore.bind(this));
		this.addListener('after', 'Or', this.afterOr.bind(this));
	}
	
	getAutomaton() {
		let context = this.stack.pop();
		context.end.terminal = this.lexeme;
	}

	beforeRegex(node:AstNode) {
		this.stackSize.push(0);
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

			let right:Context = this.stack.pop();
			let left:Context = this.stack.pop();
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

			let grouped:Context = new Context(left.start, left.end);
			this.stack.push(grouped);

			items -=1;
		}
	}

	afterCharSequence(node:AstNode) {
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
	}
		
	afterRange(node:AstNode) {
        node = node.getFirstChild();
        let children:Array<AstNode> = node.getChildrenOfType(RegexGrammar.Char);
        let low:string = children[0].asToken().text;
        let up:string = children[1].asToken().text;
        let start = new State();
        let end = new State();
        start.addTransition(CharConstraint.inRange(low, up)).target = end;
        end.terminal = this.lexeme;
        this.stack.push(new Context(start, end));
    }

    afterChar(node:AstNode) {
        let c = node.asToken().text[0];
        let start = new State();
        let end = new State();
        start.addTransition(CharConstraint.eq(c)).target = end;
        end.terminal = this.lexeme;
        this.stack.push(new Context(start, end));
    }

    afterAnyChar(node:AstNode) {
        let start = new State();
        let end = new State();
        end.terminal = this.lexeme;
        start.addTransition(CharConstraint.any()).target = end;
        this.stack.push(new Context(start, end));
    }

    afterOptional(node:AstNode) {
        let context = this.stack[this.stack.length-1];
        let skipTransition:Transition = context.start.addFallback();
        skipTransition.target = context.end;
    }

    afterZeroOrMore(node:AstNode) {
        let context = this.stack[this.stack.length-1];
		context.start.terminal = this.lexeme; // having nothing is ok
		for(let t of context.end.incoming) {
			t.target = context.start;
		}
        context.end = context.start;
    }

	afterOneOrMore(node:AstNode) {
        let context = this.stack[this.stack.length-1];
		let n2 = new State();
		for(let t of context.start.outgoing) {
			n2.addTransition(t.constraint).target = t.target;
		}
		for(let t of context.end.incoming) {
			t.target = n2;
		}
        context.end = n2;
    }

    afterOr(node:AstNode) {
        let right = this.stack.pop();
        let left = this.stack.pop();
        let firstNode = new State();
        let lastNode = new State();
        for (let t of left.start.outgoing) {
            firstNode.addTransition(t.constraint).target = t.target;
		}
        for (let t of right.start.outgoing) {
            firstNode.addTransition(t.constraint).target = t.target;
        }
		left.start.disconnect();
        right.start.disconnect();
		for(let t of left.end.incoming) {
			t.target = lastNode;
		}
		for(let t of right.end.incoming) {
			t.target = lastNode;
		}
        right.end.disconnect();
        left.end.disconnect();
        lastNode.terminal = this.lexeme;
        this.stack.push(new Context(firstNode, lastNode));
    }
};