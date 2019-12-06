import { AstNode } from "./AstNode";

export class Visitor {

	listeners:Map<String, Array<(_:AstNode)=>void>>;
	
	addListener(beforeOrAfter:string, ruleName:string, listener:(_:AstNode)=>void) {
		if(!this.listeners.has(ruleName)) {
			this.listeners.set(ruleName, new Array());
		}
		this.listeners.get(ruleName).push(listener);
	}

};