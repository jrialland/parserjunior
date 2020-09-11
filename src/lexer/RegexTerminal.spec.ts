import { regexParser, RegexVisitor, RegexTerminal } from './RegexTerminal';
import {Automaton} from '../lexer/automaton/Automaton';
import { SingleChar } from './SingleChar';
import { nodeToGraphviz } from '../parser/AstNode';

function testRegexAutomaton(expr:string) : Automaton {
    let n = regexParser.parseString(expr); 
    console.log(nodeToGraphviz(n));
    let v = new RegexVisitor(new SingleChar('x'));
    v.visit(n);
    let a = v.getAutomaton();
    console.log(a.toGraphviz());
    return a;
}

test("Simple regex", ()=> {
    testRegexAutomaton("\"0x\"(('0'~'9')|('a'~'f'))+"); // hex string
});

test("Another regex", ()=> {
    testRegexAutomaton("\"foo\"\"bar\"?");
});

test("Any String", ()=> {
    let a = testRegexAutomaton(".+");
    expect(Array.from(a.allStates()).length).toBe(2);
    expect(a.getState(1).finalState).toBeTruthy();
    expect(a.getState(1).incoming.length).toBe(2);
    expect(a.getState(1).outgoing.length).toBe(1);
});

test("forbid empty", () => {
    try {
        let t = new RegexTerminal("maybeEmpty", ".*");
        fail("we should not be able to create such regex");
    } catch(e) {
        // ok !
    }
});