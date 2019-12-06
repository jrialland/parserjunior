import { Terminal } from "../common/Terminal";
import { Automaton, AutomatonBuilder } from "./automaton/Automaton";
import {CharConstraint} from "./CharConstraint";

export class QuotedString extends Terminal {

	private _startChar:string;

	private _endChar:string;

	private _escapeChar:string;

	private _forbiddenChars:string;

	private _automaton:Automaton;

	constructor(startChar:string, endChar:string, escapeChar:string, forbiddenChars:string) {
		super("QuotedString");
		this._startChar = startChar;
		this._endChar = endChar;
		this._escapeChar = escapeChar;
		this._forbiddenChars = forbiddenChars;

		let builder = AutomatonBuilder.forTokenType(this);
		let inString = builder.newNonFinalState();
		let escaping = builder.newNonFinalState();
		let initialState = builder.initialState();
		builder.addTransition(initialState, CharConstraint.eq(this._startChar), inString);
		builder.addTransition(inString, CharConstraint.eq(this._escapeChar), escaping);
		builder.addTransition(inString, CharConstraint.inList(this._forbiddenChars), builder.failedState());
		builder.addTransition(inString, CharConstraint.not(CharConstraint.eq(this._endChar)), inString);
		builder.addTransition(escaping, CharConstraint.any(), inString);
		builder.addTransition(inString, CharConstraint.eq(this._endChar), builder.newFinalState());
		this._automaton = builder.build();
	}
	
	get automaton():Automaton {
		return this._automaton;
	}
};