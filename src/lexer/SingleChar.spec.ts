import { SingleChar } from "./SingleChar";


test('SingleChar automaton', () => {
    let s = new SingleChar('a');
    let a = s.automaton;
    expect(a.initialState.outgoing.length).toBe(1);
    expect(a.initialState.outgoing[0].target.finalState).toBe(true);
    expect(a.initialState.outgoing[0].constraint.matches('a')).toBe(true);
    expect(a.initialState.outgoing[0].constraint.matches('b')).toBe(false);
    expect(a.initialState.outgoing[0].target.terminal).toBe(s);

    console.log(s.automaton.toGraphviz());


});