import { testGrammar } from "./sampleGrammar"

test('Grammar rules id are sequential', () => {

    // The list of rules must start with id 0 and ids must be sequentials

    let id = 0;
    for (let rule of testGrammar.rules) {
        expect(rule.id).toBe(id);
        id++;
    }

    expect(testGrammar.rules[0] == testGrammar.targetRule);

});

test('Grammar getRuleById', () => {
    for (let rule of testGrammar.rules) {
        expect(testGrammar.getRuleById(rule.id)).toBe(rule);
    }
});