import {LexerStream} from './LexerStream';
import {SingleChar} from './SingleChar';
import { Reader } from '../common/Reader';

let terms = [
    new SingleChar('A'),
    new SingleChar('B'),
];

let ignored = [
    new SingleChar(' ')
];


test('Simple lexer', () => {
    let lex:LexerStream = new LexerStream(Reader.fromString('BA AB'), terms , ignored);
    for(let token of lex) {
        process.stdout.write(token.text);
    }
});