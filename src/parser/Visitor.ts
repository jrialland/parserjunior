import { AstNode } from "./AstNode";
import { Rule } from "./Rule";


class ListenerData {
	before:Array<(_:AstNode)=>void> = [];
	after:Array<(_:AstNode)=>void> = [];
};

export class Visitor {

	private listeners:Map<number, ListenerData> = new Map;
	
	addListener(beforeOrAfter:string, rule:Rule, listener:(_:AstNode)=>void) {
		if(!this.listeners.has(rule.id)) {
			this.listeners.set(rule.id, new ListenerData);
		}
		let data = this.listeners.get(rule.id);
		if(beforeOrAfter === 'before') {
			data.before.push(listener);
		} else if(beforeOrAfter === 'after') {
			data.after.push(listener);
		} else {
			throw new Error(`Illegal phase : ${beforeOrAfter}`);
		}
	}

	private notify(beforeOrAfter:string, node:AstNode) {
		if(node.rule) {
			let data = this.listeners.get(node.rule.id);
			if(data) {
				if(beforeOrAfter === 'before') {
					console.log('debug', `before  ${node.rule.name}`);
					for(let listener of data.before) {	
						listener(node);
					}
				} else if(beforeOrAfter === 'after') {
					console.log('debug', `after  ${node.rule.name}`);
					for(let listener of data.after) {
						listener(node);
					}
				}
			}
		}
	}

	visit(node:AstNode) {
		this.notify('before', node);
		node.children.forEach(c=>this.visit(c));
		this.notify('after', node);
	}
};