import { QuotedString } from "./QuotedString";
import { State, Automaton } from "./automaton/Automaton";


test('QuotedString automaton', () => {
    let s = new QuotedString('"','"', '\\', '');
    console.log(s.automaton.toGraphviz());
});