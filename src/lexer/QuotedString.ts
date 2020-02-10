import { Terminal } from "../common/Terminal";
import { Automaton, AutomatonBuilder } from "./automaton/Automaton";
import {CharConstraint} from "./CharConstraint";
import { checkServerIdentity } from "tls";

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
		this._forbiddenChars = forbiddenChars + this._endChar;
		let builder = AutomatonBuilder.forTokenType(this);

		let initial = builder.initialState();
		let inString = builder.newNonFinalState();
		let endOfString = builder.newFinalState();
		
		builder.addTransition(initial, CharConstraint.eq(this._startChar), inString);
		builder.addTransition(inString, CharConstraint.eq(this._endChar), endOfString);

		let escaping = builder.newNonFinalState();
		builder.addTransition(inString, CharConstraint.eq(this._escapeChar), escaping);
		builder.addTransition(escaping, CharConstraint.inList(this._forbiddenChars), inString);
		inString.addFallback().target = inString;
		this._automaton = builder.build();
	}
	
	get automaton():Automaton {
		return this._automaton;
	}
};