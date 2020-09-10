
import jsesc = require('jsesc');
const escape = (s: string) => jsesc(s, { 'quotes': 'double' });

/**
 * Representation of the expressions that are generated to implement the transitions
 * between lexer states. This class holds a lambda that may be used to actually check a character,
 * and also a simple string representation of the expression that may be used when generating a
 * lexer for another language
 * <ul>
 * <li>LTE</li>
 * <li>GTE</li>
 * <li>ANY</li>
 * <li>INLIST</li>
 * <li>NOT</li>
 * <li>AND</li>
 * <li>OR</li>
 * <li>EQ</li>
 * </ul>
 */
export class CharConstraint {

	/** String representation of the constraint's expression */
	private _repr: string;

	/** function that checks a character */
	private _matcher: (c: string) => boolean;

	matches(c: string) {
		return this._matcher(c);
	}

	toString() {
		return this._repr;
	}

	/** The character is matched if :
	 *  - its codepoint is greater or equal to the lower bound
	 *  - its codepoint is lower of equal to the uper bound
	 */
	static inRange(low: string, up: string): CharConstraint {
		let constraint = new CharConstraint();
		constraint._repr = `AND(GTE("${escape(low)}"), LTE("${escape(up)}"))`;
		constraint._matcher = (c) => c.charCodeAt(0) >= low.charCodeAt(0) && c.charCodeAt(0) <= up.charCodeAt(0);
		return constraint;
	}

	/** match if the character equals to the given one */
	static eq(char: string): CharConstraint {
		let constraint = new CharConstraint();
		constraint._repr = `EQ("${escape(char)}")`;
		constraint._matcher = (c) => c == char;
		return constraint;
	}

	/** matched any characted (i.e. always return true) */
	static any(): CharConstraint {
		let constraint = new CharConstraint();
		constraint._repr = "ANY";
		constraint._matcher = (c) => true;
		return constraint;
	}

	/** matches if the given char belongs to the list */
	static inList(chars: string): CharConstraint {
		let constraint = new CharConstraint();
		constraint._repr = `INLIST("${escape(chars)}")`;
		constraint._matcher = (c) => chars.indexOf(c) != -1;
		return constraint;
	}

	/** negation */
	static not(c1: CharConstraint): CharConstraint {
		let constraint = new CharConstraint();
		constraint._repr = `NOT(${c1._repr})`;
		constraint._matcher = (c) => !c1._matcher(c);
		return constraint;
	}

	/* logical and operator */
	static and(c1: CharConstraint, c2: CharConstraint): CharConstraint {
		let constraint = new CharConstraint();
		constraint._repr = `AND(${c1._repr},${c2._repr})`;
		constraint._matcher = (c) => c1._matcher(c) && c2._matcher(c);
		return constraint;
	}

	/** logical or operator */
	static or(c1: CharConstraint, c2: CharConstraint): CharConstraint {
		let constraint = new CharConstraint();
		constraint._repr = `OR(${c1._repr},${c2._repr})`;
		constraint._matcher = (c) => c1._matcher(c) || c2._matcher(c);
		return constraint;
	}
}