import {
	Grammar,
	NonTerminal,
	SingleChar,
	getFirstItemSet,
	getAllItemSets
} from './parser';

describe('Grammar', () => {

	const S = new NonTerminal("S");
	const N = new NonTerminal("N");
	const E = new NonTerminal("E");
	const V = new NonTerminal("V");
	const x = new SingleChar('x');
	const eq = new SingleChar('=');
	const star = new SingleChar('*');

	let grammar = new Grammar();

	//1. S → N
	grammar.defineRule(S, [N]);

	//2. N → V = E
	grammar.defineRule(N, [V, eq, E]);

	//3. N → E
	grammar.defineRule(N, [E]);

	//4. E → V
	grammar.defineRule(E, [V]);

	//5. V → x
	grammar.defineRule(V, [x]);

	//6. V → * 
	grammar.defineRule(V, [star, E]);


    describe('first itemSet', () => {
        it('kernel size', () => {
			let i0 = getFirstItemSet(grammar, grammar.getTargetRule());
			expect(i0.kernel.size).toBe(1);
        });
    });

	describe('all itemSets', () => {
		it('print', () => {
			let itemSets = getAllItemSets(grammar, grammar.getTargetRule());
			for(let itemSet of itemSets) {
				console.log(itemSet);
			}
		});
	});

});
