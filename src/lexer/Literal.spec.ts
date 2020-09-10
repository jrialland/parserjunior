import { Literal } from "./Literal";


test('Literal automaton', () => {
    let s = new Literal('keyword');

    console.log(s.automaton.toGraphviz());
});