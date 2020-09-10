import { LexerStream } from './LexerStream';
import { SingleChar } from './SingleChar';
import { Literal } from './Literal';
import { CCharacter } from './CCharacter';
import { Reader } from '../common/Reader';
import { QuotedString } from './QuotedString';
import { Eof } from '../common/SpecialTerminal';
import { Terminal } from '../common/Terminal';

function expectToken(lex: LexerStream, tokenType: Terminal, matchedText: string) {
    let n = lex.next();
    expect(n.done).toBe(false);
    expect(n.value.tokenType).toBe(tokenType);
    expect(n.value.text).toBe(matchedText);
}

function expectEndsWithEof(lex: LexerStream) {
    let n = lex.next();
    expect(n.done).toBe(false);
    expect(n.value.tokenType).toBe(Eof);
    n = lex.next();
    expect(n.done).toBe(true);
}

test('Simple lexer', () => {
    let a = new SingleChar('A');
    let b = new SingleChar('B');
    let lex: LexerStream = new LexerStream(Reader.fromString('BA AB'), [a, b], [
        new SingleChar(' ')
    ]);
    expectToken(lex, b, 'B');
    expectToken(lex, a, 'A');
    expectToken(lex, a, 'A');
    expectToken(lex, b, 'B');
    expectEndsWithEof(lex);

});


test("Lexer - Literals", () => {
    let hello = new Literal('Hello');
    let world = new Literal('World');
    let lex: LexerStream = new LexerStream(Reader.fromString('Hello World'), [
        hello,
        world
    ], [
        new SingleChar(' ')
    ]);

    expectToken(lex, hello, 'Hello');
    expectToken(lex, world, 'World');
    expectEndsWithEof(lex);
});

test("Lexer - CCharacter", () => {
    let lex: LexerStream = new LexerStream(Reader.fromString("'X'"), [
        CCharacter,
    ], [
        new SingleChar(' ')
    ]);
    expectToken(lex, CCharacter, "'X'");
    expectEndsWithEof(lex);
})

test("Lexer - QuotedString", () => {
    let comma = new SingleChar(',');
    let str = new QuotedString('"', '"', '\'', "\n\r")
    let lex: LexerStream = new LexerStream(Reader.fromString('"Hello"'), [
        str
    ], [
        new SingleChar(' '),
        comma
    ]);

    expectToken(lex, str, '"Hello"');
    expectEndsWithEof(lex);
});

test("Lexer - precedence", () => {
    let str = new QuotedString("'", "'", '\'', "\n\r")
    let lex: LexerStream = new LexerStream(Reader.fromString("'a','Hello'"), [
        CCharacter,
        str
    ], [
        new SingleChar(',')
    ]);
    expectToken(lex, CCharacter, "'a'");
    expectToken(lex, str, "'Hello'");
    expectEndsWithEof(lex);
});