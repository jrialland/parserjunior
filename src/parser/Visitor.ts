import { AstNode } from "./AstNode";


class ListenerData {
	before:Array<(_:AstNode)=>void> = [];
	after:Array<(_:AstNode)=>void> = [];
};

export class Visitor {

	private listeners:Map<String, ListenerData>;
	
	addListener(beforeOrAfter:string, ruleName:string, listener:(_:AstNode)=>void) {
		if(!this.listeners.has(ruleName)) {
			this.listeners.set(ruleName, new ListenerData);
		}
		let data = this.listeners.get(ruleName);
		if(beforeOrAfter === 'before') {
			data.before.push(listener);
		} else if(beforeOrAfter === 'after') {
			data.after.push(listener);
		} else {
			throw new Error(`Illegal phase : ${beforeOrAfter}`);
		}
	}

	private notify(beforeOrAfter:string, ruleName:string, node:AstNode) {
		let data = this.listeners.get(ruleName);
		if(data) {
			if(beforeOrAfter === 'before') {
				for(let l of data.before) {
					l(node);
				}
			} else if(beforeOrAfter === 'after') {
				for(let l of data.after) {
					l(node);
				}
			}
		}
	}

	visit(node:AstNode) {
		this.notify('before', node.rule.name, node);
		for(let child of node.getChildren()) {
			this.visit(child);
		}
		this.notify('after', node.rule.name, node);
	}
};