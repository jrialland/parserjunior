import {LexerStream} from './LexerStream';
import {SingleChar} from './SingleChar';
import { Reader } from '../common/Reader';
import { doesNotReject } from 'assert';
import { Eof } from '../common/SpecialTerminal';

let terms = [
    new SingleChar('A'),
    new SingleChar('B'),
];

let ignored = [
    new SingleChar(' ')
];


test('Simple lexer', () => {
    let lex:LexerStream = new LexerStream(Reader.fromString('BA AB'), terms , ignored);

    let n = lex.next();
    expect(n.done).toBe(false);
    expect(n.value.tokenType.name).toBe('B');

    n = lex.next();
    expect(n.done).toBe(false);
    expect(n.value.tokenType.name).toBe('A');

    n = lex.next();
    expect(n.done).toBe(false);
    expect(n.value.tokenType.name).toBe('A');

    n = lex.next();
    expect(n.done).toBe(false);
    expect(n.value.tokenType.name).toBe('B');

    n = lex.next();
    expect(n.done).toBe(false);
    expect(n.value.tokenType).toBe(Eof);

    n = lex.next();
    expect(n.done).toBe(true);
    expect(n.value).toBeNull();

});