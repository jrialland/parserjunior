import { Token } from '../common/Token';
import { Rule } from './Rule';

export interface AstNode {
    rule:Rule;
    children:Array<AstNode>;
    asToken():Token;
};