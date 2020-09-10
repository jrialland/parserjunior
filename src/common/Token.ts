import { Terminal } from "./Terminal";
import { Position } from "../common/Position";

export class Token {

    private _terminal: Terminal;

    private _position: Position;

    private _text: string;

    constructor(terminal: Terminal, position: Position, matched: string) {
        this._terminal = terminal;
        this._position = position;
        this._text = matched;
    }

    get text(): string {
        return this._text;
    }

    get tokenType(): Terminal {
        return this._terminal;
    }

    get position(): Position {
        return this._position;
    }
};