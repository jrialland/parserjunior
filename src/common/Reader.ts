
export class Reader {

    private _position:number;

    private _str:string;

    read():string {
        if(this._position == this._str.length) {
            return null;
        } else {
            let c = this._str[this._position];
            this._position += 1;
            return c;
        }
    }

    unshift():void {
        this._position = this._position - 1;
    }

    static fromString(s:string):Reader {
        let r = new Reader;
        r._str = s;
        r._position = 0;
        return r;
    }
}