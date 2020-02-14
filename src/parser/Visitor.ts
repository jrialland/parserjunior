import { AstNode } from "./AstNode";
import { logger } from "../util/logging";
import { Rule } from "./Rule";


class ListenerData {
	before:Array<(_:AstNode)=>void> = [];
	after:Array<(_:AstNode)=>void> = [];
};

export class Visitor {

	private listeners:Map<Rule, ListenerData> = new Map;
	
	addListener(beforeOrAfter:string, rule:Rule, listener:(_:AstNode)=>void) {
		if(!this.listeners.has(rule)) {
			this.listeners.set(rule, new ListenerData);
		}
		let data = this.listeners.get(rule);
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
			let data = this.listeners.get(node.rule);
			if(data) {
				if(beforeOrAfter === 'before') {
					logger.log('debug', `before  ${node.rule.name}`);
					for(let listener of data.before) {	
						listener(node);
					}
				} else if(beforeOrAfter === 'after') {
					logger.log('debug', `after  ${node.rule.name}`);
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