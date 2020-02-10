import { Literal } from "./Literal";
import { State, Automaton } from "./automaton/Automaton";


test('Literal automaton', () => {
    let s = new Literal('keyword');

    console.log(s.automaton.toGraphviz());
});