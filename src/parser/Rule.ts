
import {ParseSymbol} from '../common/ParseSymbol';
import {ActionType} from './ActionTable'
import { AstNode } from './AstNode';


/**
 * A grammar rule (i.e a 'production'), with a target (start) symbol, and the list
 * of the symbol that make its definition
 */
export class Rule {

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

    name:string;

    reduceAction:(node:AstNode)=>void;

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

    setReduceAction(callback:(node:AstNode)=>void) {
        this.reduceAction = callback;
    }

    getReduceAction():(node:AstNode)=>void {
        return this.reduceAction;
    }

    withName(name:string) {
        this.setName(name);
        return this;
    }

    setName(name:string) {
        this.name = name;
    }

    getName() {
        return this.name;
    }
};