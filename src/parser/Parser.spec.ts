import { Parser } from "./Parser";
import { ActionTable } from "./ActionTable";
import { testGrammar } from "./sampleGrammar";
import { Grammar } from "./Grammar";
import { NonTerminal } from "../common/NonTerminal";
import { SingleChar } from "../lexer/SingleChar";
import { toGraphviz } from "./AstNode";

/*
      0. S → N
      1. N → V '=' E
      2. N → E
      3. E → V
      4. V → 'x'
      5. V → '*' E
*/
test('Can parse', () => {
    let actionTable = new ActionTable(testGrammar);
    console.log(actionTable.asAsciiTable());
    let parser = new Parser(actionTable);
    let rootNode = parser.parseString('x = * x');
    expect(rootNode.rule.id).toBe(0);
    expect(rootNode.children[0].rule.id).toBe(1);
    expect(rootNode.children[0].children[0].rule.id).toBe(4);
    expect(rootNode.children[0].children[0].asToken().text).toBe('x');
    // etc...
});

test('oneOrMore', ()=>{
    let g = new Grammar;
    let target = new NonTerminal("target");
    g.setTargetRule(
        g.defineRule(target, [g.oneOrMore(new SingleChar('c'))]).withName('target')
    );

    let actionTable = new ActionTable(g);
    console.log(actionTable.asAsciiTable());

    let parser = new Parser(new ActionTable(g));
    let node = parser.parseString("ccccccccc");
    console.log(toGraphviz(node));

});