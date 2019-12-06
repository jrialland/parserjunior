
import { Position } from './Position';

test('Position update', () => {
    let p = Position.start();
    expect(p.column).toBe(1);
    expect(p.line).toBe(1);
    p = p.updated('a');
    expect(p.line).toBe(1);
    expect(p.column).toBe(2);
    p = p.updated('\n');
    expect(p.column).toBe(1);
    expect(p.line).toBe(2);
});