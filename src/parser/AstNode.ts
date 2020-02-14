import { Token } from '../common/Token';
import { Rule } from './Rule';
import jsesc = require('jsesc');

export interface AstNode {
    rule:Rule;
    children:Array<AstNode>;
    asToken():Token;
};

export function toGraphviz(node:AstNode) {
    let s = 'digraph AST {\n';

    let getName = (n:AstNode)=>{
        let name;
        if(n.rule) {
            name = n.rule.target.name;
        } else {
            let token = n.asToken();
            name = token.tokenType.name + jsesc(token.text);
        }
        return '"' + jsesc(name, {'quotes':'double'}) + '"';
    }

    let dump = (n:AstNode):string => {
        let s = '';
        let name = getName(n);
        for(let child of n.children) {
            s = `    ${name} -> ${getName(child)}\n`;
        }
        for(let child of n.children) {
            s += dump(child);
        }
        return s;
    }
    return 'digraph Ast {\n' + dump(node) + "}";
}