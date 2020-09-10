import { Terminal } from "../common/Terminal";
import { Automaton, AutomatonBuilder } from "./automaton/Automaton";
import { CharConstraint } from "./CharConstraint";

export class QuotedString extends Terminal {

	private _startChar: string;

	private _endChar: string;

	private _escapeChar: string;

	private _forbiddenChars: string;

	private _automaton: Automaton;

	constructor(startChar: string, endChar: string, escapeChar: string, forbiddenChars: string) {
		super("QuotedString");
		this._startChar = startChar;
		this._endChar = endChar;
		this._escapeChar = escapeChar;
		this._forbiddenChars = forbiddenChars;


		let builder = AutomatonBuilder.forTokenType(this);
		let start = builder.initialState();

		//must begin with the 'start' char
		let inString = builder.newNonFinalState();
		builder.addTransition(start, CharConstraint.eq(startChar), inString);

		let end = builder.newFinalState();

		//get out if we encounter the 'end' char
		builder.addTransition(inString, CharConstraint.not(CharConstraint.inList(forbiddenChars + endChar + escapeChar)), inString);
		builder.addTransition(inString, CharConstraint.eq(endChar), end);

		//escaping
		let escaping = builder.newNonFinalState();

		let constraint = forbiddenChars.length ? CharConstraint.not(CharConstraint.inList(forbiddenChars)) : CharConstraint.any();
		builder.addTransition(inString, CharConstraint.eq(escapeChar), escaping);
		builder.addTransition(escaping, constraint, inString);

		// otherwise continue reading the string
		//inString.addFallback().target = inString;

		this._automaton = builder.build();
	}

	get automaton(): Automaton {
		return this._automaton;
	}
};