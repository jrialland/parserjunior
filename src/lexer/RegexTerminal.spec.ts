import { regexGrammar, RegexVisitor } from './RegexTerminal';
import { Parser } from '../parser/Parser';
import { ActionTable } from '../parser/ActionTable';
import { toGraphviz } from '../parser/AstNode';

test("Regex Grammar rules",()=>{
    console.log(regexGrammar.toString());
});


test("Visit Regex", ()=> {
    let parser = new Parser(new ActionTable(regexGrammar));
    let node = parser.parseString("'a'*");
    console.log(toGraphviz(node));
})