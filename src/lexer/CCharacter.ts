import { Terminal } from "../common/Terminal";
import { Automaton, AutomatonBuilder, State } from "./automaton/Automaton";
import { CharConstraint } from "./CharConstraint";

let addHexQuad = (builder:AutomatonBuilder, origin:State):State => {
	let current:State = origin;
	for(let i=0; i<4; i++) {
		let from:State = current;
		current = builder.newNonFinalState();
		builder.addTransition(from, CharConstraint.inList('0123456789abcdefABCDEF'), current);
	}
	return current;
};

let makeAutomaton = (tokenType:Terminal) => {
	let builder = AutomatonBuilder.forTokenType(tokenType);
	let init:State = builder.initialState();
	let gotFirstQuote:State = builder.newNonFinalState();
	let escaped:State = builder.newNonFinalState();
	let octalEscape:State = builder.newNonFinalState();
	let hexEscape:State = builder.newNonFinalState();
	let octalEscape2:State = builder.newNonFinalState();
	let gotChar:State = builder.newNonFinalState();
	let universalEscape:State = builder.newNonFinalState();
	let gotHexQuad:State;
	let gotHexQuad2:State;
	let done:State = builder.newFinalState();

	builder.addTransition(init, CharConstraint.eq('\''), gotFirstQuote);
	builder.addTransition(gotFirstQuote,CharConstraint.eq('\\'), escaped);

	builder.addTransition(
			gotFirstQuote,
			CharConstraint.and(
					CharConstraint.inRange(' ', String.fromCharCode(128)), // 'displayable ascii chars, ie between 0x20 and 0xEF
					CharConstraint.not(CharConstraint.eq('\\'))
			),
			gotChar);

	builder.addTransition(escaped,CharConstraint.inList("\"?abfnrtv\\"), gotChar);

	builder.addTransition(escaped, CharConstraint.inList('01234567'), octalEscape);
	builder.addTransition(octalEscape, CharConstraint.inList('01234567'), octalEscape2);
	builder.addTransition(octalEscape, CharConstraint.eq('\''), done);
	builder.addTransition(octalEscape2, CharConstraint.inList('01234567'), gotChar);
	builder.addTransition(octalEscape2, CharConstraint.eq('\''), done);

	builder.addTransition(escaped, CharConstraint.eq('x'), hexEscape);
	builder.addTransition(escaped, CharConstraint.inList('0123456789abcdefABCDEF'), hexEscape);
	builder.addTransition(hexEscape, CharConstraint.eq('\''), done);

	builder.addTransition(escaped, CharConstraint.inList('uU'), universalEscape);
	gotHexQuad = addHexQuad(builder, universalEscape);
	builder.addTransition(gotHexQuad, CharConstraint.eq('\''), done);
	gotHexQuad2 = addHexQuad(builder, gotHexQuad);
	builder.addTransition(gotHexQuad2, CharConstraint.eq('\''), done);

	builder.addTransition(gotChar, CharConstraint.eq('\''), done);
	return builder.build();
};

class _CCharacter extends Terminal {

	private _automaton:Automaton;

	constructor() {
		super("CCharacter");
		this._automaton = makeAutomaton(this);		
	}

	get automaton() {
		return this._automaton;
	}
};

export const CCharacter:Terminal = new _CCharacter();
