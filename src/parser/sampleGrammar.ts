import { ParseSymbol } from "../common/ParseSymbol";
import { NonTerminal } from "../common/NonTerminal";
import { Eof } from "../common/SpecialTerminal";
import { SingleChar } from "../lexer/SingleChar";
import { Grammar } from "./Grammar";

export let testGrammarSymbols:Map<string, ParseSymbol> = new Map;
testGrammarSymbols.set('S', new NonTerminal('S'));
testGrammarSymbols.set('N', new NonTerminal('N'));
testGrammarSymbols.set('V', new NonTerminal('V'));
testGrammarSymbols.set('E', new NonTerminal('E'));
testGrammarSymbols.set('x', new SingleChar('x'));
testGrammarSymbols.set('=', new SingleChar('='));
testGrammarSymbols.set('*', new SingleChar('*'));
testGrammarSymbols.set('eof', Eof);

function makeGrammar() {

    let S = testGrammarSymbols.get('S');
    let N = testGrammarSymbols.get('N');
    let V = testGrammarSymbols.get('V');
    let E = testGrammarSymbols.get('E');

    let x = testGrammarSymbols.get('x');
    let eq = testGrammarSymbols.get('=');
    let star = testGrammarSymbols.get('*');
    
    let g = new Grammar();
    
    // S → N
    g.defineRule(S, [N]);
    
    // N → V = E
    g.defineRule(N, [V, eq, E]);
    
    // N → E
    g.defineRule(N, [E]);
    
    // E → V
    g.defineRule(E, [V]);
    
    // V → x
    g.defineRule(V, [x]);
    
    // V → * E
    g.defineRule(V, [star, E]);

    return g;
}

export let testGrammar = makeGrammar();