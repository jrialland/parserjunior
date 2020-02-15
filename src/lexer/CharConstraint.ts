
import jsesc = require('jsesc');

export class CharConstraint {

	private _repr:string;

	private _matcher:(c:string) => boolean;

	matches(c:string) {
		return this._matcher(c);
	}

	toString() {
		return this._repr;
	}

	static inRange(low:string, up:string):CharConstraint {
		let constraint = new CharConstraint();
		constraint._repr = "AND(GTE(" + jsesc(low) +"), LT(" + jsesc(up) +"))";
		constraint._matcher = (c) => c.charCodeAt(0) >= low.charCodeAt(0) && c.charCodeAt(0) < up.charCodeAt(0);
		return constraint;
	}

	static eq(char:string):CharConstraint {
		let constraint = new CharConstraint();
		constraint._repr = "EQ(\"" + jsesc(char) + "\")";
		constraint._matcher = (c) => c == char;
		return constraint;
	}

	static any():CharConstraint {
		let constraint = new CharConstraint();
		constraint._repr = "ANY";
		constraint._matcher = (c) => true;
		return constraint;
	}

	static inList(chars:string):CharConstraint {
		let constraint = new CharConstraint();
		constraint._repr = "INLIST(\"" + jsesc(chars) + "\")";
		constraint._matcher = (c) => chars.indexOf(c) != -1;
		return constraint;
	}

	static not(c1:CharConstraint):CharConstraint {
		let constraint = new CharConstraint();
		constraint._repr = "NOT" + constraint._repr + ")";
		constraint._matcher = (c) => !c1._matcher(c);
		return constraint;
	}

	static and(c1:CharConstraint, c2:CharConstraint):CharConstraint {
		let constraint = new CharConstraint();
		constraint._repr = "AND(" + c1._repr + ", " + c2._repr + ")";
		constraint._matcher = (c) => c1._matcher(c) && c2._matcher(c);
		return constraint;
	}
}