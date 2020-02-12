
import {ParseSymbol} from '../common/ParseSymbol';
import {NonTerminal} from '../common/NonTerminal';
import {Empty, Eof} from '../common/SpecialTerminal';
import {Rule} from './Rule';
import {ActionType} from './ActionTable';

/**
 * A grammar is a set of rules, for which a parser can be generated.
 */
export class Grammar {

    /* The main rule of the grammar */
    targetRule:Rule;
    
    /** rules */
    rules:Array<Rule>;

    symbols:Map<string, ParseSymbol>;

    precedenceLevels:Map<string, number>;

    nameCounter:number;

    constructor() {
        this.targetRule = null;
        this.rules = [];
        this.symbols = new Map();
        this.nameCounter = 0;
    }

    /**
     * Adds a new rule to the grammar.
     * @param targetSymbol The left part of the new rule
     * @param definition The right part of the new rule
     */
    defineRule(targetSymbol:NonTerminal, definition:Array<ParseSymbol>):Rule {
        return this.addRule(new Rule(targetSymbol, definition));
    }

    /**
     * Adds a new rule
     */
    addRule(r:Rule) {
        
        //update symbols
        this.symbols.set(r.target.getUid(), r.target);
        for(let s of r.definition) {
            this.symbols.set(s.getUid(), s);
        }

        // add rule to the list
        this.rules.push(r);

        // set the rule's uid
        r.setId(this.rules.length-1);

        //consider as target rule if it was not defined yet
        if(this.targetRule == null) {
            this.setTargetRule(r);
        }

        return r;
    }

    /**
     * 
     * @param rule the rule to consider as target rule.
     * Note : this rule should already be part of the known rules of this grammar, therefore addRule() or defineRule() must have been called first.
     */
    setTargetRule(rule:Rule) {
        this.targetRule = rule
        // update the list of rules so the target rule is always id=0
        this.targetRule.id = 0;
        let newRules:Array<Rule> = [];
        newRules.push(this.targetRule);
        let i = 1;
        for(let rule of this.rules) {
            if(rule != this.targetRule) {
                rule.id = i++;
                newRules.push(rule);
            }
        }
        this.rules = newRules;
    }

    /**
     * @return the main rule of this grammar
     */
    getTargetRule():Rule {
        if(this.targetRule == null) {
            if(this.rules.length > 0) {
                this.targetRule = this.rules[0];
            }
        }
        return this.targetRule;
    }

    getRules():Array<Rule> {
        return this.rules;
    }

    getRuleById(ruleIndex:number):Rule {
        return this.rules[ruleIndex];
    }

    getSymbols():IterableIterator<ParseSymbol> {
        return this.symbols.values();
    }

    getTerminals():ParseSymbol[] {    
        let a :Array<ParseSymbol>= [];
        for(const s of this.getSymbols()) {
            if(s.isTerminal()) {
                a.push(s);
            }
        }
        return a;
    }

    getNonTerminals():ParseSymbol[] {
        let a :Array<ParseSymbol>= [];
        for(const s of this.getSymbols()) {
            if( ! s.isTerminal()) {
                a.push(s);
            }
        }
        return a;
    }

    getSymbolPrecedence(sym:ParseSymbol):number {
        let val = this.precedenceLevels.get(sym.getUid());
        return val == null ? 0 : val;
    }

    getConflictResolutionHint(rule:Rule, sym:ParseSymbol) {
        let decision:ActionType = null;
        let rulePrecedence = rule.getPrecedenceLevel();
        let tokenPrecedence = this.getSymbolPrecedence(sym);
        if(tokenPrecedence > rulePrecedence) {
            return ActionType.Shift;
        } else if( rulePrecedence > tokenPrecedence) {
            return ActionType.Reduce;
        } else {
            return rule.getConflictArbitration();
        }
    }

    oneOf(...syms:ParseSymbol[]):ParseSymbol {
        if(syms.length < 2) {
			throw Error("The list must contain at list 2 symbols");
		}
        let name = "oneOf(" + (syms.map(s=>s.name).join(', ')) + ")";
        let tmp = new NonTerminal(name);
        for(let s of syms) {
            this.defineRule(tmp, [s]);
        }
        return tmp;
    }

    optional(...syms:ParseSymbol[]):ParseSymbol {
        let name = "optional(" + (syms.map(s=>s.name).join(', ')) + ")";
        let opt = new NonTerminal(name);
        this.defineRule(opt, syms);
        this.defineRule(opt, [Empty]);
        return opt;
    }

    zeroOrMore(...syms:ParseSymbol[]):ParseSymbol {
        let name = "zeroOrMore_" + this.nameCounter++;
        let tmp = new NonTerminal(name);
        this.defineRule(tmp, syms); // zeroOrMore(a) -> x y z
        let list = new Array<ParseSymbol>();
        list.push(tmp);
        for(let s of syms) {
            list.push(s);
        }
        this.defineRule(tmp, list); // zeroOrMore(a) -> zeroOrMore(a) x y z
        this.defineRule(tmp, [Empty]); // zeroOrMore(a) -> empty
        return tmp;
    }

    oneOrMore(...syms:ParseSymbol[]):ParseSymbol {
        let name = "oneOrMore_" + this.nameCounter++;
        let tmp = new NonTerminal(name);
        this.defineRule(tmp, syms); // oneOrMore(a) -> x y z
        let list = new Array<ParseSymbol>();
        list.push(tmp);
        for(let s of syms) {
            list.push(s);
        }
        this.defineRule(tmp, list); //oneOrMore(a) -> oneOrMore(a) x y z
        return tmp;
    }

    listOf(typeOfItems:ParseSymbol, separator:ParseSymbol, allowEmptyLists:boolean):ParseSymbol {
        let tmp = new NonTerminal("listOf(" + typeOfItems.toString() + ")");

        // a list may contain only one item
        this.defineRule(tmp, [typeOfItems]);

        this.defineRule(tmp, [tmp, separator, typeOfItems]).setReduceAction((parser, lexerStream, node) => {
            let list = node.getFirstChild().getChildren();
            list.push(node.getLastChild());
            node.setChildren(list);
        });

        // a list may be empty
        if(allowEmptyLists) {
            this.defineRule(tmp, [Empty]);
        }

        return tmp;
    }

};