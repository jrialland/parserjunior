import { Position } from "../common/Position";


export class LexicalError extends Error {

    private _position: Position;

    constructor(message: string, position: Position) {
        super(`[${position.line}:${position.column}] : ${message}`);
        this._position = position;
    }
};