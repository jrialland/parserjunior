
import { ParseSymbol } from '../common/ParseSymbol';
import { Terminal } from '../common/Terminal';
import { Eof, Empty } from '../common/SpecialTerminal';
import { Rule } from './Rule';
import { Grammar } from './Grammar';
import { Z_FULL_FLUSH } from 'zlib';
import { isFlowPredicate } from '@babel/types';
import { resolvePlugin } from '@babel/core';

/**
 * type of parser action
 */
export enum ActionType {
    Shift,
    Reduce,
    Goto,
    Accept,
    Fail
};

//------------------------------------------------------------------------------
/**
 * Extended symbols are a construction that is used when computing the
 * parse table of a grammar using the lalr(1) algorithm.
 */
class ExtendedSymbol extends ParseSymbol {
    
    /* The state we came from */
    from:number;
    
    /* Wrapped symbol */
    sym:ParseSymbol;

    /* The next state */
    to:number;

    constructor(from:number, sym:ParseSymbol, to:number) {
        super('('+from+','+sym.name+','+to+')')
        this.from = from;
        this.sym = sym;
        this.to = to;
    }

    /** An ExtendedSymbol is terminal if its wrapped symbol is terminal */
    isTerminal() {
        return this.sym.isTerminal();
    }

    /*
    * unique id of an ExtendedSymbol
    */
    getUid() {
        return '('+this.from+','+this.sym.getUid()+','+this.to+')';
    }

    /* @return the wrapped symbol */
    asSimpleSymbol() {
        return this.sym;
    }
};

//------------------------------------------------------------------------------
/**
 * The notion of 'Extended rule' is used in the definition of 'Extended grammars'
 */
class ExtendedRule extends Rule {

    /** the rule that is extended */
    baseRule:Rule;
    
    constructor(baseRule:Rule, target:ExtendedSymbol, definition:Array<ExtendedSymbol>) {
        super(target, definition);
        this.baseRule = baseRule;
    }
    
    /** checks if this rule is an extension of the given rule */
    isExtensionOf(rule:Rule):boolean {
        return this.baseRule == rule;
    }
    
    /**
     * @return the last state of the rightmost extended symbol of the definition.
     */
    getFinalState():number {
        const eSym = this.definition[this.definition.length-1] as ExtendedSymbol;
        return eSym.to;
    }
};

//------------------------------------------------------------------------------


//------------------------------------------------------------------------------
/**
 * Syntax analysis : An 'Item' is a rule that have a 'pointer' on one member of its definition (i.e its right side)
 */
class Item {

    /** the rule for this item */
    rule:Rule;

    /** the index in the definition we point to */
    pointer:number;
    
    constructor(rule:Rule, pointer:number) {
        this.rule = rule;
        this.pointer = pointer;
    }

    /** @return the symbol that we point to */
    getExpectedSymbol():ParseSymbol {
        let def = this.rule.definition;
        if(this.pointer == def.length) {
            return null;
        } else {
            return def[this.pointer];
        }
    }

    /** Almost the same representation as a Rule, except that we mark the pointed symbol with a dot */
    toString():string {
        let s = this.rule.target.toString();
        s += " → ";
        let i = 0;
        for(let symbol of this.rule.definition) {
            if(i>0) {
                s += ' ';
            }
            if(i == this.pointer) {
                s += "• ";
            }
            s += symbol.toString();
            i++;
        }
        if(this.pointer ==  this.rule.definition.length) {
            s += " •";
        }
        return s;
    }

    /**
     * Gets the new item, i.e the item for the same rule that points on the next symbol on the right side;
     */
    shift():Item {
        return new Item(this.rule, this.pointer+1);
    }

    /**
     * @return The unique id of the item.
     */
    getUid():string {
        return ''+ this.rule.id + ',' + this.pointer;
    }
};

//------------------------------------------------------------------------------
/**
 * As we sometimes have to compare sets of items, we have a way
 * to compute a sort of 'hash' for a thoses sets, so we tell if 2 sets contain the same 
 * items or not.
 */
function uidOfKernel(s:Set<Item>):string {
    let list = []
    for(let item of s) {
        list.push(item.getUid());
    }
    // sort the list because the order in the set can vary
    list.sort()
    return list.join('-');
}

//------------------------------------------------------------------------------
/** 
 * A set of items
 */
class ItemSet {
    
    /** unique id */
    id:number;
    
    /** the 'kernel' of this set */
    kernel:Set<Item>;
    
    /** The extra rules that form the set */
    members:Set<Item>;
    
    /**
     * links to other itemSets depending on input symbol (key=symbol uids)
     */
    transitions:Map<string, ItemSet>;
    
    possibleNexts:Set<ParseSymbol>;

    constructor(id:number, kernel:Set<Item>, members:Set<Item>) {
        this.id = id;
        this.kernel = kernel;
        this.members = members;
        this.transitions = new Map();
        this.possibleNexts = null;
    }

    /** get the symbols that would be valid if we want to continue validating the grammar's rules */
    possibleNextSymbols():Set<ParseSymbol> {
        if(this.possibleNexts == null) {
            this.possibleNexts = new Set();
            this.forAllItems( (item) => {
                const expected = item.getExpectedSymbol();
                if(expected != null) {
                    this.possibleNexts.add(expected);
                }
                return true;
            });
        }
        return this.possibleNexts;
    }

    addTransition(symbol:ParseSymbol, targetItemSet:ItemSet) {
        this.transitions.set(symbol.getUid(), targetItemSet);
    }

    *getItemsThatExpect(expected:ParseSymbol):IterableIterator<Item> {
        for(const set of [this.kernel, this.members]) {
            for(const item of set) {
                const expectedS = item.getExpectedSymbol();
                if(expectedS == expected) {
                    yield item;
                }
            }
        }
    }

    getTransitionFor(s:ParseSymbol) {
        return this.transitions.get(s.getUid());
    }

    forAllItems(callback:(i:Item)=>boolean) {
        for(const item of this.kernel) {
            if(!callback(item)) {
                return;
            }
        }
        for(const item of this.members) {
            if(!callback(item)) {
                return;
            }
        }
    }

    toString():string {
        let s = '';
        for(const item of this.kernel) {
            s += item.toString()+'\n';
        }
        for(const item of this.members) {
            s += ' + ' + item.toString()+'\n';
        }
        return s.replace(/\n$/, '');
    }

};

//------------------------------------------------------------------------------

function extendItemSetKernel(grammar:Grammar, kernel:Set<Item>) {

    const map:Map<string, Item> = new Map();
    const stack = [];
    
    for(const item of kernel) {
        stack.push(item);
    }

    while(stack.length) {
        const currentItem = stack.pop();
        const expected = currentItem.getExpectedSymbol();
        if(expected != null) {
            grammar
                .getRules()
                .filter(r => r.target == expected)
                .forEach(r => {
                        const i = new Item(r, 0);
                        const uid = i.getUid();
                        if(!(uid in map.keys)) {
                            map.set(uid, i);
                            stack.push(i);
                        }
                });
        }
    }

    for(const item of kernel) {
        map.delete(item.getUid());
    }

    return new Set(map.values());
}

//------------------------------------------------------------------------------
/** Create the first itemSet 'i0' */
export function getFirstItemSet(grammar:Grammar, targetRule:Rule):ItemSet {
    const firstItem = new Item(targetRule, 0);
    const kernel = new Set([firstItem]);
    return  new ItemSet(0, kernel, extendItemSetKernel(grammar, kernel));
}

//------------------------------------------------------------------------------
/**
 * Computes all the item Sets from a given start rule
 * @param grammar 
 */
export function getAllItemSets(grammar:Grammar, startRule:Rule):Set<ItemSet> {
    
    const i0 = getFirstItemSet(grammar, startRule);

    const knownKernels:Map<string, ItemSet> = new Map();
    knownKernels.set(uidOfKernel(i0.kernel), i0);
    
    const stack = [i0];
    let i = 1;

    while(stack.length) {
       
        const currentItemSet:ItemSet = stack.pop();
        
        for(const symbol of currentItemSet.possibleNextSymbols()) {
        
            const newKernel:Set<Item> = new Set([]);
        
            for(let item of currentItemSet.getItemsThatExpect(symbol)) {
                newKernel.add(item.shift());
            }
        
            if(newKernel.size > 0) {
                const uidOfNewKernel = uidOfKernel(newKernel);
                let nitemSet:ItemSet = knownKernels.get(uidOfNewKernel);
                if(nitemSet == null) {
                    nitemSet = new ItemSet(i++, newKernel, extendItemSetKernel(grammar, newKernel));
                    knownKernels.set(uidOfNewKernel, nitemSet);
                    stack.push(nitemSet);
                }
                currentItemSet.addTransition(symbol, nitemSet);
            }
        }
    }
    return new Set(knownKernels.values());
}

//------------------------------------------------------------------------------


//------------------------------------------------------------------------------
/**
 * Parser's actions
 */
export class Action {
    type:ActionType;
    target:number;
    constructor(type:ActionType, target:number) {
        this.type = type;
        this.target = target;
    }
    get typeStr() {
        switch(this.type) {
            case ActionType.Accept:
                    return 'Accept';
            case ActionType.Fail:
                    return 'Fail';
            case ActionType.Goto:
                    return 'Goto';
            case ActionType.Reduce:
                    return 'Reduce';
            case ActionType.Shift:
                return 'Shift';
            default:
                throw new Error('unknown ActionType ' + this.type);
        }
    }
};

//------------------------------------------------------------------------------
export function getTranslationTable(grammar:Grammar,  allItemSets:Set<ItemSet>):Map<number, Map<string, number>> {
    let table:Map<number, Map<string, number>> = new Map();
    for(let itemSet of allItemSets) {
        let currentState = itemSet.id;
        let row:Map<string, number> = new Map();
        table.set(currentState, row);
        for(let sym of grammar.getSymbols()) {
            let targetItemSet:ItemSet = itemSet.getTransitionFor(sym);
            if(targetItemSet != null) {
                row.set(sym.getUid(), targetItemSet.id);
            }
        }
    }
    return table;
}

//------------------------------------------------------------------------------
export function initializeShiftsAndGotos(actionTable:ActionTable, translationTable:Map<number, Map<string, number>>) {
    for(let [state, line] of translationTable.entries()){
        for(let [symUid, targetState] of line) {
            let sym = actionTable.getSymbolForUid(symUid);
            if(sym.isTerminal()) {
                actionTable.setAction(state, sym, new Action(ActionType.Shift, targetState), false);
            } else {
                actionTable.setAction(state, sym, new Action(ActionType.Goto, targetState), false);
            }
        }
    }
}
 
//------------------------------------------------------------------------------
/**
 * Gives the parsing action depending on the input symbol and the parser's internal state
 */
export class ActionTable {
    
    grammar:Grammar;

    symbols:Map<string, ParseSymbol>;

	table:Map<number, Map<string, Action>>;

    constructor(grammar:Grammar, compute?:boolean) {
        compute = typeof(compute) === 'undefined' ? true : compute;
        this.grammar = grammar;
        this.symbols = new Map();
        this.table = new Map();
        for(let s of grammar.getSymbols()) {
            this.symbols.set(s.getUid(), s);
        }
        if(compute) {
            let allItemSets:Set<ItemSet> = getAllItemSets(grammar, grammar.getTargetRule());
            let translationTable = getTranslationTable(grammar, allItemSets);
            initializeShiftsAndGotos(this, translationTable);
            initializeReductions(grammar, this, allItemSets);
            initializeAccept(grammar, this, allItemSets);
        }
    }

    getSymbolForUid(uid:string):ParseSymbol {
        return this.symbols.get(uid);
    }

    setAction(state:number, sym:ParseSymbol, action:Action, allowReplace:boolean) {
		let symUid = sym.getUid();
		let row:Map<string, Action>;
		if(!this.table.has(state)) {
			row = new Map();
			this.table.set(state, row);
		} else {
            row = this.table.get(state);
        }
		if(!allowReplace && row.has(symUid) && row.get(symUid) != action) {
			let oldActionType = row.get(symUid).type;
			let err = "Unresolved " + oldActionType + "/" + action.type + " conflict\n";
			err += "    For state : " + state + "\n";
			err += "    For symbol : " + sym.toString() + "\n";
			err += "    Action 1 : " + row.get(symUid) + "\n";
			err += "    Action 2 : " + action + "\n";
			throw new Error(err);
		} else {
			row.set(symUid, action);
		}
    }

    getAction(state:number, sym:ParseSymbol):Action {
        let row = this.table.get(state);
		if(row) {
			return row.get(sym.getUid());
		} else {
			throw new Error("Not a state : " + state);
		}
    }

	getNextState(currentState:number, symbol:ParseSymbol):number {
		let row = this.table.get(currentState)
		if(row != null) {
			let gotoAction = row.get(symbol.getUid());
			if(gotoAction) {
				return gotoAction.target;
			}
		}
		throw new Error(`No GOTO Action for state '${currentState}', Symbol '${symbol}'`);
	}

	getExpectedTerminals(state:number) : Array<Terminal> {
        let terminals:Array<Terminal> = [];
        let row = this.table.get(state);
        if(row) {
            for(let uid of row.keys()) {
                let sym = this.getSymbolForUid(uid);
                if(sym.isTerminal()) {
                    terminals.push(sym as Terminal);
                }
            }
            
        }
        return terminals;
	}
}

//------------------------------------------------------------------------------
/** builds an 'extended' grammar from the item sets */
export function makeExtendedGrammar(targetRule:Rule, allItemSets:Set<ItemSet>):Grammar {
    let extendedSymbols:Map<string, ExtendedSymbol> = new Map;
    let eGrammar = new Grammar;
    let eTargetRule:ExtendedRule = null;
    for(let itemSet of allItemSets) {
        let initialState = itemSet.id;
        itemSet.forAllItems((item:Item) => {
            if(item.pointer == 0) {
                let rule:Rule = item.rule;
                let currentItem = itemSet;
                let eClause:Array<ExtendedSymbol> = [];
                for(let s of rule.definition) {
                    let start = currentItem.id;
                    currentItem = currentItem.getTransitionFor(s);
                    let end = currentItem.id;
                    let extSym = new ExtendedSymbol(start, s, end);
                    let key = extSym.getUid();
                    if(extendedSymbols.has(key)) {
                        extSym = extendedSymbols.get(key);
                    } else {
                        extendedSymbols.set(key, extSym);
                    }
                    eClause.push(extSym);
                }
                let finalState=-1;
                let transition:ItemSet = itemSet.getTransitionFor(rule.target);
                if(transition) {
                    finalState = transition.id;
                }
                let eTarget:ExtendedSymbol = new ExtendedSymbol(initialState, rule.target, finalState);
                let key = eTarget.getUid();
                if(extendedSymbols.has(key)) {
                    eTarget = extendedSymbols.get(key);
                } else {
                    extendedSymbols.set(key, eTarget);
                }
                let eRule:ExtendedRule = new ExtendedRule(rule, eTarget, eClause);
                if(rule == targetRule) {
                    eTargetRule = eRule;
                }
                eGrammar.addRule(eRule);

            }
            return true;
        });
    }
    eGrammar.setTargetRule(eTargetRule);
    return eGrammar;
}

//------------------------------------------------------------------------------
function resolveConflict(grammar:Grammar, rule:Rule, sym:ParseSymbol, existing:Action, reduceAction:Action) {
    if(existing.type == ActionType.Accept || existing.type == ActionType.Reduce) {
        // 'Accept' always win
        return existing;
    }
    if(existing.type == ActionType.Shift) {
        let preference = grammar.getConflictResolutionHint(rule, sym);
        if(preference != null) {
            if(preference == ActionType.Fail) {
                return null;
            } else if(preference == ActionType.Shift) {
                return existing;
            } else if(preference == ActionType.Reduce) {
                return reduceAction;
            }
        } else {
            return existing;
        }
    }
}

//------------------------------------------------------------------------------
abstract class SymbolSet {
    subject:ParseSymbol;
    resolution:Set<ParseSymbol> = new Set;
    definition:Set<SymbolSet> = new Set;
    constructor(sym:ParseSymbol) {
        this.subject = sym;
    }
    abstract get typeName():string;

        
    toString() {
        return this.asString();
    }

    asString(simple?:boolean):string {
        let s = this.typeName + '(' + this.subject.toString() + ')';
        if(simple) {
            return s;
        }
        s += '    definition = { ' + Array.from(this.definition).map(f=>f.asString(true)).join(', ') + '}';
        return s;
    }
};
//------------------------------------------------------------------------------
class FirstSet extends SymbolSet {
    constructor(sym:ParseSymbol, resolution:Set<ParseSymbol>) {
        super(sym);
    }
    get typeName() {
        return 'FIRST';
    }
};

//------------------------------------------------------------------------------
class FollowSet extends SymbolSet {

    constructor(sym:ParseSymbol) {
        super(sym);
    }

    addToDefinition(symbolSet:SymbolSet) {
        this.definition.add(symbolSet);
    }

    get typeName() {
        return 'FOLLOW';
    }
};

//------------------------------------------------------------------------------
export function getFIRST(grammar:Grammar, sym:ParseSymbol):Set<ParseSymbol> {
    // 1. FIRST(terminal) = [terminal]
    if(sym.isTerminal()) {
        return new Set([sym.asSimpleSymbol()]);
    }
    let set = new Set<ParseSymbol>([]);
    for(let r of grammar.getRules().filter(r=>r.target==sym)) {
        if(r.definition[0].isTerminal()) {
            //2. if the definition starts with a terminal, the set is this terminal
            set.add(r.definition[0].asSimpleSymbol());
            continue;
        }
        let brk=false;
        for(let s2 of r.definition) {
            if(sym != s2) {
                let a = getFIRST(grammar, s2);
                let containedEmpty = a.delete(Empty);
                for(let item of a) {
                    set.add(item);
                }
                // 3a. if First(x) did not contain ε, we do not need to contine scanning
                if(!containedEmpty) {
                    brk = true;
                    break;
                }
            }
        }
        // 3b. every First(x) contained ε, so we have to add it to the set
        if(!brk) {
            set.add(Empty);
        }
    }
    return set;
}

export function defineFOLLOW(allSets:Map<string, FollowSet>, grammar:Grammar, D:ParseSymbol) {
    let followSet = allSets.get(D.getUid());
    // Construct for the rule have the form R → a* D b.
    for(let rule of grammar.getRules()) {
        let R = rule.target;
        let followSetOfR = allSets.get(R.getUid());
        //for each occurence of D in the clause
        for(let i=0, max=rule.definition.length-1; i < max; i++) { //minus 1 because if D is the last item of the definition we dont care (i.e 'b' must exist)
            if(rule.definition[i] == D) {
                let b = rule.definition[i+1];
                //Everything in First(b) (except for ε) is added to Follow(D)
                let f = getFIRST(grammar, b);
                let containedEmpty = f.delete(Empty);
                let firstSet = new FirstSet(b, f);
                followSet.addToDefinition(firstSet);
                if(containedEmpty) {
                    followSet.addToDefinition(followSetOfR);
                }
            }
        }
        //Finally, if we have a rule R → a* D, then everything in Follow(R) is placed in Follow(D).
        if(rule.definition.length>0 && D == rule.definition[rule.definition.length-1]) {
            followSet.addToDefinition(followSetOfR);
        }
    }
}

export function computeFOLLOWSets(grammar:Grammar):Map<string, FollowSet> {

    let map:Map<string, FollowSet> = new Map;

    //Create a FollowSet for each symbol (terminal/nonterminal)
    for(let sym of grammar.getSymbols()) {
        map.set(sym.getUid(), new FollowSet(sym));
    }

    //Place an End of Input token ($) into the starting rule's follow set.
    let targetUid = grammar.getTargetRule().target.getUid();
    map.get(targetUid).resolution.add(Eof);

    //Define FOLLOW(X) for all non-terminals
    for(let sym of grammar.getNonTerminals()) {
        defineFOLLOW(map, grammar, sym);
    }

    return map;
}

//------------------------------------------------------------------------------
export function initializeReductions(grammar:Grammar, actionTable:ActionTable, allItemSets:Set<ItemSet>) {
    let extendedGrammar:Grammar = makeExtendedGrammar(grammar.getTargetRule(), allItemSets);

    
}

//------------------------------------------------------------------------------
export function initializeAccept(grammar:Grammar, actionTable:ActionTable, itemSets:Set<ItemSet>) {
    let targetRule = grammar.getTargetRule();
    let accept = new Action(ActionType.Accept, 0);
    let allParsed = new Item(targetRule, targetRule.definition.length);
    for(let itemSet of itemSets) {
        itemSet.forAllItems(item => {
            if(item.getUid() === allParsed.getUid()) {
                actionTable.setAction(itemSet.id, Eof, accept, true);
                return false;
            }
            return true;
        });
    }
}
