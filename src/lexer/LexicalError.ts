import { Position } from "../common/Position";

export class LexicalError extends Error {

    private _position:Position;

    constructor(unexpected:string, position:Position) {
        super("Unexpected character : '" + unexpected + "'");
        this._position = position;
    }   
};