//------------------------------------------------------------------------------
/**
 * Base class for all types of symbols
 */
class ParseSymbol {

    /** Should be unique for a given grammar */
    name:string;

    constructor(name:string) {
        this.name = name;
    }

    /* whether the symbol is terminal (i.e a lexer token) or not */
    isTerminal() :boolean {
        throw new Error("unimplemented");
    }

    /**
     * @returns the name
     */
    toString():string {
        return this.name;
    }

    /**
     * The unique id of a symbol, should be unique for a given grammar.
     */
    getUid():string {
        return this.name;
    }

    /*
     * Due the notion of 'Extended symbol', this method returns the 'normal' symbol
     * associated with the current symbol. the ExtendedSymbol class overrides this method.
     */
    asSimpleSymbol():ParseSymbol {
        return this;
    }
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
/* Any symbol that is not a terminal (i.e any symbol that does not represent a token) */
export class NonTerminal extends ParseSymbol {
    constructor(name:string) {
        super(name)
    }

    isTerminal() :boolean {
        return false;
    }

};

//------------------------------------------------------------------------------
/*
 * This specialization of the NonTerminal class to make the distinction between 'real' tokens
 * and some interal tokens that have a special meaning
 */
class SpecialTerminal extends ParseSymbol {

    constructor(name:string){
        super(name)
    }

    /** @return true */
    isTerminal() {
        return true;
    }
};

//------------------------------------------------------------------------------
/**
 * The Terminal that represents the end of input
 */
const Eof = new SpecialTerminal('Eof');

/**
 * The terminal that represents
 */
const Empty = new SpecialTerminal('ɛ');

//------------------------------------------------------------------------------
/*
 * Simple token that represents a single character
 */
export class SingleChar extends ParseSymbol {

    constructor(c:string) {
        super(c);
    }

    isTerminal() :boolean {
        return true;
    }

    toString() :string {
        return "'" + this.name + "'";
    }

};

class ParserContext {

};

class AstNode {

};

//------------------------------------------------------------------------------
/**
 * A grammar rule (i.e a 'production'), with a target (start) symbol, and the list
 * of the symbol that make its definition
 */
class Rule {

    /** The unique id of the rule */
    id:number;

    /**
     * The start symbol for this rule
     */
    target:ParseSymbol;
    
    /**
     * The list of symbols (right part of the rule)
     */
    definition:Array<ParseSymbol>;
    
    precedenceLevel:number;

    conflictArbitration:ActionType;

    reduceAction:(_:ParserContext)=>void;

    constructor(target:ParseSymbol, definition:Array<ParseSymbol>) {
        this.target = target;
        this.definition = definition;
    }
    
    toString() {
        let s:string = this.target.toString() + " →";
        for(const item of this.definition) {
            s += " " + item.toString();
        }
        return s;
    }

    /** sets rule's uid, which should be unique for a given grammar */
    setId(id:number) {
        this.id = id;
    }

    getPrecedenceLevel():Number {
        return this.precedenceLevel;
    }

    setPrecedenceLevel(level:number) {
        this.precedenceLevel = level;
    }

    getConflictArbitration():ActionType {
        return this.conflictArbitration;
    }

    setConflictArbitration(conflictArbitration:ActionType) {
        this.conflictArbitration = conflictArbitration;
    }

    setReduceAction(callback:(_ParserContext:any)=>void) {
        this.reduceAction = callback;
    }

    getReduceAction():(_:ParserContext)=>void {
        return this.reduceAction;
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
    defineRule(targetSymbol:NonTerminal, definition:Array<ParseSymbol>) {
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
        r.setId(this.rules.length);

        //consider as target rule if it was not defined yet
        if(this.targetRule == null) {
            this.setTargetRule(r);
        }

        return r;
    }

    /**
     * 
     * @param rule the rule to consider as target rule.
     * Note : this rule should already be part of the known rules of this grammar, therefor addRule() or defineRule() must have been called first.
     */
    setTargetRule(rule:Rule) {
        this.targetRule = rule
    }

    /**
     * @return the main rule of this grammar
     */
    getTargetRule():Rule {
        return this.targetRule;
    }

    getRules():Array<Rule> {
        return this.rules;
    }

    getSymbols():IterableIterator<ParseSymbol> {
        return this.symbols.values();
    }

    *getTerminals():IterableIterator<ParseSymbol> {    
        for(const s of this.getSymbols()) {
            if(s.isTerminal()) {
                yield s;
            }
        }
    }

    *getNonTerminals():IterableIterator<ParseSymbol> {
        for(const s of this.getSymbols()) {
            if( ! s.isTerminal()) {
                yield s;
            }
        }
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
        this.defineRule(tmp, syms);
        let list = new Array<ParseSymbol>();
        list.push(tmp);
        for(let s of syms) {
            list.push(s);
        }
        this.defineRule(tmp, list); // zeroOrMore(a) -> zeroOrMore(a) x y z
        this.defineRule(tmp, [Empty]); // zeroOrMore(a) -> empty
        return tmp;
    }

    listOf(typeOfItems:ParseSymbol, separator:ParseSymbol, allowEmptyLists:boolean):ParseSymbol {
        let tmp = new NonTerminal("listOf(" + typeOfItems.toString() + ")");

        // a list may contain only one item
        this.defineRule(tmp, [typeOfItems]);

        this.defineRule(tmp, [tmp, separator, typeOfItems]).setReduceAction((context) => {
            let targetNode = context.getAstNode();
            let list = targetNode.getFirstChild().getChildren();
            list.push(targetNode.getLastChild());
            targetNode.setChildren(list);
        });

        // a list may be empty
        if(allowEmptyLists) {
            this.defineRule(tmp, [Empty]);
        }

        return tmp;
    }

};

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
            if(i == this.pointer) {
                s += "•";
            }
            s += (" " + symbol.toString());
            i++;
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
/**
 * type of parser action
 */
enum ActionType {
    Shift,
    Reduce,
    Goto,
    Accept,
    Fail
};

//------------------------------------------------------------------------------
/**
 * Parser's actions
 */
class Action {
    type:ActionType;
    target:number;
    constructor(type:ActionType, target:number) {
        this.type = type;
        this.target = target;
    }
};

//------------------------------------------------------------------------------
function getTranslationTable(grammar:Grammar,  allItemSets:Set<ItemSet>):Map<number, Map<string, number>> {
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
function initializeShiftsAndGotos(actionTable:ActionTable, translationTable:Map<number, Map<string, number>>) {
    translationTable.forEach( (value, state, map) => {
         value.forEach( (target, symUid, map_) => {
             let sym = actionTable.getSymbolForUid(symUid);
             if(sym.isTerminal()) {
                 actionTable.setAction(state, sym, new Action(ActionType.Shift, target), false);
             } else {
                 actionTable.setAction(state, sym, new Action(ActionType.Goto, target), false);
             }
         });
    });
 }
 
//------------------------------------------------------------------------------
/**
 * Gives the parsing action depending on the input symbol and the parser's internal state
 */
class ActionTable {
    
    grammar:Grammar;

    symbols:Map<string, ParseSymbol>;

    constructor(grammar:Grammar) {
        
        this.grammar = grammar;
        
        this.symbols = new Map();
        for(let s of grammar.getSymbols()) {
            this.symbols.set(s.getUid(), s);
        }

        let allItemSets:Set<ItemSet> = getAllItemSets(grammar, grammar.getTargetRule());
        let translationTable = getTranslationTable(grammar, allItemSets);
        initializeShiftsAndGotos(this, translationTable);
        initializeReductions(grammar, this, allItemSets);
        initializeAccept(grammar, this, allItemSets);
    }

    getSymbolForUid(uid:string):ParseSymbol {
        return this.symbols.get(uid);
    }

    //state, s, new Action(ActionType.Shift, entry.getValue()), false
    setAction(state:number, sym:ParseSymbol, action:Action, allowReplace:boolean) {

    }

    getAction(state:number, sym:ParseSymbol):Action {
        return null;//FIXME
    }
}

//------------------------------------------------------------------------------
/** builds an 'extended' grammar from the item sets */
function makeExtendedGrammar(targetRule:Rule, allItemSets:Set<ItemSet>) {

    const extendedSymbols:Map<string, ExtendedSymbol> = new Map();
    const eGrammar = new Grammar();
    
    
    for(let itemSet of allItemSets) {

        const initialState = itemSet.id;
        itemSet.forAllItems( (item) => {

            if(item.pointer == 0) {
                
                const rule = item.rule;
                let currentItem = itemSet;
                const eClause:Array<ExtendedSymbol> = new Array();

                for(let s of rule.definition) {
                    const start = currentItem.id;
                    currentItem = currentItem.getTransitionFor(s);
                    const end = currentItem.id;
                    const extSym = new ExtendedSymbol(start, s, end);
                    const extSymUid = extSym.getUid();
                    if( ! extendedSymbols.has(extSymUid)) {
                        extendedSymbols.set(extSymUid, extSym);
                    }
                    eClause.push(extSym);
                }
                const transition = itemSet.getTransitionFor(rule.target);
                const finalState = transition == null ? -1 : transition.id;
                const eTarget = new ExtendedSymbol(initialState, rule.target, finalState);
                const eTargetUid = eTarget.getUid();
                if( ! extendedSymbols.has(eTargetUid)) {
                    extendedSymbols.set(eTargetUid, eTarget);
                }
                eGrammar.addRule(new ExtendedRule(rule, eTarget, eClause));
                return true;
            }
        });
    }

    for(const r of eGrammar.getRules().map((r) => r as ExtendedRule)) {
        const eTarget = r.target as ExtendedSymbol;
        if(eTarget.sym.getUid() === targetRule.target.getUid()) {
            eGrammar.setTargetRule(r);
            return eGrammar;
        }
    }
    
    throw Error("Definition of extended grammar failed")
}

//------------------------------------------------------------------------------
/**
 * base class for the 'FirstSet' and 'FollowSet' classes
 */
class SymbolSet {

    subject:ParseSymbol;

    resolution:Set<ParseSymbol>;

    composition:Set<SymbolSet>;

    constructor(subject:ParseSymbol) {
        this.subject = subject;
        this.resolution = null;
        this.composition = new Set();
    }

    setResolution(resolution:Set<ParseSymbol>) {
        this.resolution = resolution;
    }

    setResolved() {
        this.resolution = new Set();
        for(let ls of this.composition) {
            for(let item of ls.resolution) {
                this.resolution.add(item);
            }
        }
    }

    add(ls:SymbolSet) {
        this.composition.add(ls);
    } 

    isResolved():boolean {
        return this.resolution != null;
    }

    /**
     * @param sets the system of sets that have to be simplified
     * @return the number of LazySets that are now fully defined
     */
    static simplify(sets:IterableIterator<SymbolSet>):number {

        /** The mechanism that allow us to simplify the system of equations */
        class SystemSolver {

            listeners:Array<(lazySet:SymbolSet)=>void>;
            
            constructor() {
                this.listeners = Array();
            }
            
            doOnCompletion(callback:(lazySet:SymbolSet)=>void) {
                this.listeners.push(callback);
            }

            notifyCompletionOf(ls:SymbolSet) {
                for(let listener of this.listeners) {
                    listener(ls);
                }
            }
        };
        const sol = new SystemSolver();

        for(const ls of sets) {

            if(! ls.isResolved()) {
                
                // toResolve is the list that has to be resolved in order to consider 'ls' as resolved
                const toResolve:Set<SymbolSet> = new Set();
                for(const s of ls.composition) {
                    if(!s.isResolved()) {
                        toResolve.add(s);
                    }
                }
                // each time a set is resolved, we will upate the list, and mark 'ls' as resolved if the set gets empty
                sol.doOnCompletion((lazySet) => {
                    toResolve.delete(lazySet);
                    if(toResolve.size == 0 && ! ls.isResolved()) {
                        ls.setResolved();
                        sol.notifyCompletionOf(ls);
                    } 
                });
            }
        }

        let count = 0;
        for(let ls of sets) {
            if(ls.isResolved()) {
                sol.notifyCompletionOf(ls);
                count += 1;
            }
        }

        return count;
    }

    /**
     * Simplifies the definition of the given sets
     * @param map a system of 'LazySets'
     */
    static resolveAll(map:Map<string, SymbolSet>) {
        // the number of sets that have to be simplified
        const total = map.size;

        // the amount that are already simplified
        let resolved = SymbolSet.simplify(map.values());
        let lastResolved = 0;

        while(resolved < total && lastResolved != resolved) {
            lastResolved = resolved;
            for(const f of map.values()) {
                f.composition.delete(f);
                for(let f2 of map.values()) {
                    if( f != f2) {
                        if(f2.composition.delete(f)) {
                            for(const item of f.composition) {
                                f2.composition.add(item);
                            }
                        }
                    }
                }
            }
            resolved = SymbolSet.simplify(map.values());
        }

        if(resolved < total) {
            //TODO : dumping the unresolved sets would give an idea on why it cannot be solved
            throw new Error("Could not compute grammar");
        }
    }
}

//------------------------------------------------------------------------------
class FirstSet extends SymbolSet {
}

//------------------------------------------------------------------------------
class FollowSet extends SymbolSet {
    static emptySet(s:ParseSymbol) {
        let f = new FollowSet(s);
        f.setResolution(new Set());
        return f;
    }
}

//------------------------------------------------------------------------------
function getFirstSet(grammar:Grammar, s:ParseSymbol):Set<ParseSymbol> {
    if(s.isTerminal()) {
        return new Set([s.asSimpleSymbol()]);
    } else {
        const set:Set<ParseSymbol> = new Set();
        for(let r of grammar.getRules()) {
            if(r.target === s) {

                // if the first symbol is a terminal, the set IS this terminal
                let firstTerminal:ParseSymbol = null;
                if(r.definition.length > 0 && (firstTerminal = r.definition[0]).isTerminal()) {
                    set.add(firstTerminal.asSimpleSymbol());
                    continue;
                } 

                // if not, we scan the symbol
                let brk = false;
                for(const s2 of r.definition) {
                    const a = getFirstSet(grammar, s2);
                    const containedEmpty = a.delete(Empty);
                    for(const element of a) {
                        set.add(element.asSimpleSymbol());
                    }
                    if(!containedEmpty) {
                        brk = true;
                        break;
                    }
                }

                // every FIRST(x) that we computed contained ε, so we have to add it
                if(!brk) {
                    set.add(Empty);
                }                              
            }
        }
        return set;
    }
}

//------------------------------------------------------------------------------
function defineFollowSet(followSets:Map<string, FollowSet>, grammar:Grammar, D:ParseSymbol) {
    let followSet = followSets.get(D.getUid());

    // Construct for the rule which have the form 'R -> a* D b.'
    for(let rule of grammar.getRules()) {

        let R:ParseSymbol = rule.target;
        for(let i=0,max=rule.definition.length; i < max; i++) {
            if(rule.definition[i].getUid() == D.getUid()) {

                let b = rule.definition[i+1];
                //Everything in FIRST(b) (expect for Eof) is added to FOLLOW(D)
                let first = getFirstSet(grammar, b);
                let didContainEmpty = first.delete(Empty);
                let firstSet = new FirstSet(b);
                firstSet.setResolution(first);
                followSet.add(firstSet);

                //If FIRST(b) contains 'Empty' the everything in FOLLOW(R) is put in FOLLOW(D)
                if(didContainEmpty) {
                    followSet.add(followSets.get(R.getUid()));
                }
            }
        }

        // Finally, if we have a rule R -> a* D, then everything in FOLLOW(R) is placed in FOLLOW(D)
        let last = rule.definition[rule.definition.length-1];
        if(rule.definition.length > 0 && D.getUid() === last.getUid()) {
            followSet.add(followSets.get(R.getUid()));
        }
    }
}

//------------------------------------------------------------------------------
function getFollowSets(grammar:Grammar):Map<string, FollowSet> {
    let map:Map<string, FollowSet> = new Map();

    for(let sym of grammar.getSymbols()) {
        if(sym.isTerminal()) {
            // The follow set of a terminal is always empty
            map.set(sym.getUid(), FollowSet.emptySet(sym));
        } else {
            //Initialize an new set for each nonterminal
            map.set(sym.getUid(), new FollowSet(sym));
        } 
    }

    //place an 'End of input' token ($) into the starting rule's follow set
    map.get(grammar.getTargetRule().target.getUid()).setResolution(new Set([Eof]));

    SymbolSet.resolveAll(map);

    for(let s of grammar.getNonTerminals()) {
        defineFollowSet(map, grammar, s);
    }
    
    return map;
}

//------------------------------------------------------------------------------
class PreMergeReduction {

    extendedRule:ExtendedRule;

    followSet:Set<ParseSymbol>;

    constructor(extendedRule:ExtendedRule, followSet:Set<ParseSymbol>) {
        this.extendedRule =extendedRule;
        this.followSet = followSet;
    }

    matches(other:PreMergeReduction) {
        let sameFinalState = this.extendedRule.getFinalState() === other.extendedRule.getFinalState();
        return sameFinalState && this.extendedRule.isExtensionOf(other.extendedRule.baseRule);
    }

    getBaseRule():Rule {
        return this.extendedRule.baseRule;
    }

    getFinalState():number {
        return this.extendedRule.getFinalState();
    }
};

class MergedReduction {
    rule:Rule;
    finalState:number;
    followSet:Set<ParseSymbol>;
    constructor(rule:Rule, finalState:number, followSet:Set<ParseSymbol>) {
        this.rule = rule;
        this.finalState = finalState;
        this.followSet = followSet;
    }
}

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
function initializeReductions(grammar:Grammar, actionTable:ActionTable, allItemSets:Set<ItemSet>) {

    let extendedGrammar:Grammar = makeExtendedGrammar(grammar.getTargetRule(), allItemSets);

    let followSets = getFollowSets(extendedGrammar);

    // step1 : build a list of tiles and their follow sets
    let step1:Array<PreMergeReduction> = new Array();
    for(let eRule of extendedGrammar.getRules()) {
        let followSet:FollowSet = followSets.get(eRule.target.getUid());
        if(!followSet.subject.isTerminal()) {
            let resolution:Set<ParseSymbol> = new Set();
            for(let ps of followSet.resolution) {
                resolution.add(ps.asSimpleSymbol());
            }
            step1.push(new PreMergeReduction(eRule as ExtendedRule, resolution));
        }
    }

    // step2 : merge some of the rules
    let step2:Map<string, MergedReduction> = new Map();
    for(let pm of step1) {
        let matching = step1.filter(r => r.matches(pm));
        if(matching.length == 1) {
            let merged = new MergedReduction(pm.getBaseRule(), pm.getFinalState(), pm.followSet);
            step2.set( ''+merged.rule.id +','+merged.finalState , merged); //FIXME key ?
        } else {
            let newFollowSet:Set<ParseSymbol> = new Set();
            for(let m of matching) {
                for(let fs of m.followSet) {
                    newFollowSet.add(fs);
                }
            }
            let merged = new MergedReduction(pm.getBaseRule(), pm.getFinalState(), newFollowSet);
            step2.set(''+merged.rule.id +','+merged.finalState, merged);
        }
    }

    // step 3 : fill the action table with the reductions
    for(let merged of step2.values()) {
        for(let s of merged.followSet) {
            let state = merged.finalState;
            let actionToInsert = new Action(ActionType.Reduce, merged.rule.id);
            let existingAction = actionTable.getAction(state, s);
            if(existingAction != null) {
                let mitigatedAction = resolveConflict(grammar, merged.rule, s, existingAction, actionToInsert);
                actionTable.setAction(state, s, mitigatedAction, true);
            } else {
                actionTable.setAction(state, s, actionToInsert, false);
            }
        }
    }
}

function initializeAccept(grammar:Grammar, actionTable:ActionTable, itemSets:Set<ItemSet>) {
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
