import {Grammar} from '../parser/Grammar';
import {NonTerminal} from '../common/NonTerminal';
import {SingleChar} from './SingleChar';
import {CCharacter} from './CCharacter';
import {Literal} from './Literal';
import {QuotedString} from './QuotedString';

export class RegexGrammar extends Grammar {

	static OneOrMoreExpr:NonTerminal = new NonTerminal('OneOrMoreExpr');
	static Regex = new NonTerminal("Regex");
	static Expr = new NonTerminal("Expr");
	static Sequence = new NonTerminal("Sequence");
	static CharacterRange = new NonTerminal("CharacterRange");

	static LeftBrace = new SingleChar('(');
	static RightBrace = new SingleChar(')');
	static Dot = new SingleChar('.');
	static Plus = new SingleChar('+');
	static Pipe = new SingleChar('|');
	static QuestionMark = new SingleChar('?');
	static Star = new SingleChar('*');
	static ThreePoints = new Literal("..");
	static Char = CCharacter;
	static SingleQuotedString = new QuotedString('\'', '\'', '\\', "\n\r");

	constructor() {
		super();
		this.defineRule(RegexGrammar.OneOrMoreExpr, [this.oneOrMore(RegexGrammar.Expr)]);
		this.setTargetRule(this.defineRule(RegexGrammar.Regex, [RegexGrammar.OneOrMoreExpr]));
		this.defineRule(RegexGrammar.Expr, [RegexGrammar.LeftBrace, RegexGrammar.OneOrMoreExpr, RegexGrammar.RightBrace]).withName("Group");
		this.defineRule(RegexGrammar.Expr, [RegexGrammar.Sequence]).withName("CharSequence");
		this.defineRule(RegexGrammar.Expr, [RegexGrammar.CharacterRange]).withName("Range");
		this.defineRule(RegexGrammar.Expr, [RegexGrammar.Char]).withName("Char");
		this.defineRule(RegexGrammar.Expr, [RegexGrammar.Dot]).withName("AnyChar");
		this.defineRule(RegexGrammar.Expr, [RegexGrammar.Expr, RegexGrammar.QuestionMark]).withName("Optional");
        this.defineRule(RegexGrammar.Expr, [RegexGrammar.Expr, RegexGrammar.Star]).withName("ZeroOrMore");
        this.defineRule(RegexGrammar.Expr, [RegexGrammar.Expr, RegexGrammar.Plus]).withName("OneOrMore");
        this.defineRule(RegexGrammar.Expr, [RegexGrammar.Expr, RegexGrammar.Pipe, RegexGrammar.Expr]).withName("Or");
		this.defineRule(RegexGrammar.CharacterRange, [RegexGrammar.Char, RegexGrammar.ThreePoints, RegexGrammar.Char]);
		this.defineRule(RegexGrammar.Sequence, [RegexGrammar.SingleQuotedString]);
	}
};
