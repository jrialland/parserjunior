
import { ParseSymbol } from '../common/ParseSymbol';
import { Terminal } from '../common/Terminal';
import { Eof, Empty } from '../common/SpecialTerminal';
import { Rule } from './Rule';
import { Grammar } from './Grammar';
import { computeFOLLOWSets } from './SymbolSet';
import {table} from 'table';
import { maxHeaderSize } from 'http';
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


    asAsciiTable():string {
        
        let sorted = Array.from(this.table.keys()).sort();
        let maxState = sorted[sorted.length-1];

        let terminals = this.grammar.getTerminals();
        terminals.push(Eof);
        let nonTerminals = this.grammar.getNonTerminals();

        let data:Array<Array<String>> = [];

        //header
        let header:Array<String> = [];
        header.push(' ');
        for(let t of terminals) {
            header.push(t.toString());
        }
        for(let nt of nonTerminals) {
            header.push(nt.toString());
        }
        data.push(header);

        //line for each state
        for(let i=0; i<=maxState; i++) {
            let line:Array<String> = [];
            line.push(''+i);

            for(let t of terminals) {
                let action = this.getAction(i, t);
                if(action) {
                    switch(action.type) {
                        case ActionType.Accept:
                            line.push('accept');
                            break;
                        case ActionType.Shift:
                            line.push('s'+action.target);
                            break;
                        case ActionType.Reduce:
                            line.push('r(' + this.grammar.getRuleById(action.target).toString() + ')');
                            break;
                    }
                } else(line.push(' '));
            }

            for(let nt of nonTerminals) {
                let action = this.getAction(i, nt);
                if(action) {
                    line.push(''+action.target);
                } else {
                    line.push(' ');
                }
            }
            data.push(line);
        }
        return table(data);
    }

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

    getActionNoCheck(state:number, sym:ParseSymbol) {
        let row = this.table.get(state);
        if(row) {
            return row.get(sym.getUid());
        }
        return null;
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
export function initializeReductions(grammar:Grammar, actionTable:ActionTable, allItemSets:Set<ItemSet>) {
    let extendedGrammar:Grammar = makeExtendedGrammar(grammar.getTargetRule(), allItemSets);
    let followSets = computeFOLLOWSets(extendedGrammar);

    // Merge rules that descend from the same original rule and have the same endpoint
    class MergedReduction {
        symbols:Set<ParseSymbol> = new Set;
        ruleId:number;
        finalSet:number;
        constructor(finalSet:number, ruleId:number) {
            this.finalSet = finalSet;
            this.ruleId = ruleId;
        }
        addSymbols(syms:Set<ParseSymbol>) {
            for(let sym of syms) {
                this.symbols.add(sym);
            }
        }
    };

    let reductions:Map<string, MergedReduction> = new Map;

    for(let r of extendedGrammar.rules) {
        let eRule = r as ExtendedRule;
        let last = eRule.definition[eRule.definition.length-1] as ExtendedSymbol;
        let endpoint = last.to;
        let mrUid = eRule.baseRule.id + '/' + endpoint;
        let mr:MergedReduction;
        if(reductions.has(mrUid)) {
            mr = reductions.get(mrUid);
        } else {
            mr = new MergedReduction(endpoint, eRule.baseRule.id);
            reductions.set(mrUid, mr)
        }
        let followSet = followSets.get(eRule.target.getUid());
        mr.addSymbols(followSet.resolution);
    }

    for(let mr of reductions.values()) {
        for(let symbol of mr.symbols) {
            //insert a reduce action
            let actionToInsert = new Action(ActionType.Reduce, mr.ruleId);
            let existingAction = actionTable.getActionNoCheck(mr.finalSet, symbol);

            if(existingAction != null) {
                let rule = grammar.getRuleById(mr.ruleId);
                let resolved = resolveConflict(grammar, rule, symbol, existingAction, actionToInsert);
                actionTable.setAction(mr.finalSet, symbol, resolved, true);
            } else {
                actionTable.setAction(mr.finalSet, symbol, actionToInsert, false);
            }
        }
    }
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
