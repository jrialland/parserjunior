import { ParseSymbol } from "../common/ParseSymbol";
import { Grammar } from "./Grammar";
import { Empty, Eof } from "../common/SpecialTerminal";

//------------------------------------------------------------------------------
abstract class SymbolSet {
    subject: ParseSymbol;
    resolution: Set<ParseSymbol> = new Set;
    definition: Set<SymbolSet> = new Set;

    constructor(sym: ParseSymbol) {
        this.subject = sym;
    }

    abstract get typeName(): string;


    toString() {
        return this.asString();
    }

    asString(simple?: boolean): string {
        let s = this.typeName + '(' + this.subject.toString() + ')';
        if (simple) {
            return s;
        }
        if (this.solved) {
            s += '    resolution = {' + Array.from(this.resolution).map(sym => sym.toString()).join(', ') + '}';
        } else {
            s += '    definition = { ' + Array.from(this.definition).map(f => f.asString(true)).join(', ') + '}';
        }
        return s;
    }

    abstract get solved(): boolean;
};

//------------------------------------------------------------------------------
class FirstSet extends SymbolSet {

    constructor(sym: ParseSymbol, res: Set<ParseSymbol>) {
        super(sym);
        this.resolution = res;
    }

    get typeName() {
        return 'FIRST';
    }

    get solved() {
        return true;
    }
};

//------------------------------------------------------------------------------
function getFIRST(grammar: Grammar, sym: ParseSymbol): Set<ParseSymbol> {
    // 1. FIRST(terminal) = [terminal]
    if (sym.isTerminal()) {
        return new Set([sym.asSimpleSymbol()]);
    }
    let set = new Set<ParseSymbol>([]);
    for (let r of grammar.getRules().filter(r => r.target == sym)) {
        if (r.definition[0].isTerminal()) {
            //2. if the definition starts with a terminal, the set is this terminal
            set.add(r.definition[0].asSimpleSymbol());
            continue;
        }
        let brk = false;
        for (let s2 of r.definition) {
            if (sym != s2) {
                let a = getFIRST(grammar, s2);
                let containedEmpty = a.delete(Empty);
                for (let item of a) {
                    set.add(item);
                }
                // 3a. if First(x) did not contain ε, we do not need to contine scanning
                if (!containedEmpty) {
                    brk = true;
                    break;
                }
            }
        }
        // 3b. every First(x) contained ε, so we have to add it to the set
        if (!brk) {
            set.add(Empty);
        }
    }
    return set;
};

//------------------------------------------------------------------------------
class FollowSet extends SymbolSet {

    solved_: boolean = false;

    constructor(sym: ParseSymbol) {
        super(sym);
    }

    addToDefinition(symbolSet: SymbolSet) {
        this.definition.add(symbolSet);
    }

    get typeName() {
        return 'FOLLOW';
    }

    get solved(): boolean {
        return this.solved_;
    }

    set solved(b: boolean) {
        this.solved_ = b;
    }
};

export function defineFOLLOW(allSets: Map<string, FollowSet>, grammar: Grammar, D: ParseSymbol) {
    let followSet = allSets.get(D.getUid());
    // Construct for the rule have the form R → a* D b.
    for (let rule of grammar.getRules()) {
        let R = rule.target;
        let followSetOfR = allSets.get(R.getUid());
        //for each occurence of D in the clause
        for (let i = 0, max = rule.definition.length - 1; i < max; i++) { //minus 1 because if D is the last item of the definition we dont care (i.e 'b' must exist)
            if (rule.definition[i] == D) {
                let b = rule.definition[i + 1];
                //Everything in First(b) (except for ε) is added to Follow(D)
                let f = getFIRST(grammar, b);
                let containedEmpty = f.delete(Empty);
                let firstSet = new FirstSet(b, f);
                followSet.addToDefinition(firstSet);
                if (containedEmpty) {
                    followSet.addToDefinition(followSetOfR);
                }
            }
        }
        //Finally, if we have a rule R → a* D, then everything in Follow(R) is placed in Follow(D).
        if (rule.definition.length > 0 && D == rule.definition[rule.definition.length - 1]) {
            followSet.addToDefinition(followSetOfR);
        }
    }
}

export function computeFOLLOWSets(grammar: Grammar): Map<string, FollowSet> {

    let map: Map<string, FollowSet> = new Map;

    // Create a FollowSet for each symbol (terminal/nonterminal)
    for (let sym of grammar.getSymbols()) {
        map.set(sym.getUid(), new FollowSet(sym));
    }

    // Place an End of Input token ($) into the starting rule's follow set.
    let targetUid = grammar.getTargetRule().target.getUid();
    let targetSet = map.get(targetUid);
    targetSet.resolution.add(Eof);
    targetSet.solved = true;

    // Define FOLLOW(X) for all non-terminals
    for (let sym of grammar.getNonTerminals()) {
        defineFOLLOW(map, grammar, sym);
    }

    // Keep only values for non-terminals
    let nonTerminals: Map<string, FollowSet> = new Map;
    for (let [uid, followSet] of map) {
        if (!followSet.subject.isTerminal()) {
            nonTerminals.set(uid, followSet);
        }
    }

    solve(nonTerminals);

    return nonTerminals;
}

function resolutionPass(map: Map<string, FollowSet>): number {
    let count = 0;
    for (let followSet of map.values()) {
        if (!followSet.solved) {
            for (let def of followSet.definition) {
                for (let sym of def.resolution) {
                    followSet.resolution.add(sym);
                }
                if (def.solved) {
                    followSet.definition.delete(def);
                }
            }
            if (followSet.definition.size == 0) {
                followSet.solved = true;
                count += 1;
            } else {
                if (followSet.definition.size == 1) {
                    let def = followSet.definition.values().next().value;
                    if (def.definition.size == 1) {
                        let def2 = def.definition.values().next().value;
                        if (def2 == followSet) {
                            followSet.definition.clear();
                            followSet.solved = true;
                            count += 1;
                        }
                    }
                }
            }
        } else {
            count += 1;
        }
    }
    return count;
}

function solve(map: Map<string, FollowSet>) {
    let lastPass = -1;
    while (true) {
        let thisPass = resolutionPass(map);

        if (thisPass == map.size) {
            // hurray !
            return;
        }

        if (lastPass == thisPass) {
            //We made no progress :(
            throw new Error("FOLLOW sets cannot be resolved ! Check your grammar !");
        }

        lastPass = thisPass;
    }
}