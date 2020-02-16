import { RegexTerminal, regexParser, RegexVisitor, regexGrammar } from './RegexTerminal';
import { SingleChar } from './SingleChar';
import { AstHelper} from '../parser/AstHelper';

test("Visit Regex", ()=> {
    let n = regexParser.parseString("\"0x\"(('0'~'9')|('a'~'f'))+"); // hex string

    console.log(AstHelper.toGraphviz(n));

    let v = new RegexVisitor(new SingleChar('x'));
    v.visit(n);
    console.log(v.getAutomaton().toGraphviz());
});