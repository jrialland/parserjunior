import { CCharacter } from "./CCharacter";

test('CCharacter', () => {
    console.log(CCharacter.automaton.toGraphviz());
});