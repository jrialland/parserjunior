
export class Position {

    private _line: number;

    private _col: number;

    static start(): Position {
        let p: Position = new Position;
        p._line = 1;
        p._col = 1;
        return p;
    }

    updated(s: string): Position {
        let p: Position = new Position;
        p._line = this._line;
        p._col = this._col;
        for (let c of s) {
            if (c == '\n') {
                p._line = p._line + 1;
                p._col = 1;
            } else {
                p._col = this._col + 1;
            }
        }
        return p;
    }

    get column(): number {
        return this._col;
    }

    get line(): number {
        return this._line;
    }
}