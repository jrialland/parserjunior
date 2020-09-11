import { Token } from '../common/Token';
import { Rule } from './Rule';
import jsesc from 'jsesc';

export interface AstNode {
    rule: Rule;
    children: Array<AstNode>;
    asToken(): Token;
    isLeaf(): boolean;
};

export function nodeToGraphviz(node: AstNode) {

    let allNodes: Set<AstNode> = new Set;

    const addNode = (n: AstNode) => {
        allNodes.add(n);
        for (let child of n.children) {
            addNode(child);
        }
    };

    const getLabel = (n: AstNode): string => {
        if (n.rule) {
            return n.rule.name;
        } else {
            return n.asToken().tokenType.name;
        }
    };

    addNode(node);


    let s = 'digraph AST {\n'
    let a = Array.from(allNodes);
    for (let i = 0; i < a.length; i++) {
        let shape = a[i].isLeaf() ? 'oval' : 'box';
        s += `    ${i} [shape=${shape},label="${jsesc(getLabel(a[i]))}"];\n`;
    }
    for (let i = 0; i < a.length; i++) {
        let currentNode = a[i];
        for (let child of currentNode.children) {
            let childId = a.indexOf(child);
            s += `    ${i} -> ${childId};\n`;
        }
    }
    s += '}';
    return s;
}