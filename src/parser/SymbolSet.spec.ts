import { getAllItemSets, makeExtendedGrammar } from "./ActionTable";
import { testGrammar } from './sampleGrammar';
import { computeFOLLOWSets } from './SymbolSet';

test('Compute FOLLOW sets', () => {
    let itemSets = getAllItemSets(testGrammar, testGrammar.getTargetRule());
    let eGrammar = makeExtendedGrammar(testGrammar.getTargetRule(), itemSets);
    let map = computeFOLLOWSets(eGrammar);
    let s = '';
    map.forEach((followSet, key) => {
        s += followSet.toString() + '\n';
    });
    //console.log(s);
});