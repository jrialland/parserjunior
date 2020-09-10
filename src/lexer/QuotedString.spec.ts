import { QuotedString } from "./QuotedString";

test('QuotedString automaton', () => {
    let s = new QuotedString('"', '"', '\\', '');
    console.log(s.automaton.toGraphviz());
});