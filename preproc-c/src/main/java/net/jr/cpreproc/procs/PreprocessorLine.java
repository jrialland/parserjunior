package net.jr.cpreproc.procs;

import net.jr.common.Position;
import net.jr.grammar.c.CStringUtil;

import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.IntStream;

public class PreprocessorLine implements CharSequence {

    private SortedMap<Integer, Position> positions = new TreeMap<>();

    private String text;

    public PreprocessorLine(SortedMap<Integer, Position> positions, String text) {
        this.positions = positions;
        this.text = text;
    }

    public PreprocessorLine(Position position, String text) {
        this.positions.put(0, position);
        this.text = text;
    }

    public PreprocessorLine(Position position) {
        this(position, "");
    }

    public Position getPosition() {
        return positions.get(0);
    }

    public Position getPosition(int offset) {
        if (offset == 0) {
            return getPosition();
        }
        Iterator<Map.Entry<Integer, Position>> it = positions.entrySet().iterator();
        Position previous = null;
        int previousOffset = 0;
        while (it.hasNext()) {
            Map.Entry<Integer, Position> entry = it.next();
            if (offset < entry.getKey()) {
                return previous.withOffset(offset - previousOffset);
            }
            previous = entry.getValue();
            previousOffset = entry.getKey();
        }
        return previous.withOffset(offset - previousOffset);
    }

    public PreprocessorLine mergeWith(PreprocessorLine t) {
        TreeMap<Integer, Position> newPositions = new TreeMap<>(positions);
        int offset = text.length();
        for (Map.Entry<Integer, Position> entry : t.positions.entrySet()) {
            newPositions.put(entry.getKey() + offset, entry.getValue());
        }
        return new PreprocessorLine(newPositions, text + t.text);
    }

    private void updatePosition(Position position) {
        if (!getPosition(text.length()).equals(position)) {
            positions.put(text.length(), position);
        }
    }

    public void extend(Position position, char c) {
        updatePosition(position);
        text += c;
    }

    public void extend(Position position, String s) {
        updatePosition(position);
        text += s;
    }

    public java.lang.String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getLine() {
        return positions.get(0).getLine();
    }

    @Override
    public int length() {
        return text.length();
    }

    @Override
    public char charAt(int index) {
        return text.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return text.subSequence(start, end);
    }

    @Override
    public IntStream chars() {
        return text.chars();
    }

    @Override
    public IntStream codePoints() {
        return text.codePoints();
    }

    public boolean isEmpty() {
        return text.isEmpty();
    }

    public void removeChars(int index, int nChars) {
        Iterator<Map.Entry<Integer, Position>> posIt = positions.entrySet().iterator();
        Map<Integer, Position> toAdd = new TreeMap<>();
        while (posIt.hasNext()) {
            Map.Entry<Integer, Position> entry = posIt.next();
            if (entry.getKey() > index) {
                posIt.remove();
                toAdd.put(entry.getKey()-nChars, entry.getValue());
            }
        }
        positions.putAll(toAdd);
        text = text.substring(0, index) + text.substring(Math.min(text.length(), index + nChars));
    }

    public void insert(int index, String s) {
        Iterator<Map.Entry<Integer, Position>> posIt = positions.entrySet().iterator();
        Map<Integer, Position> toAdd = new TreeMap<>();
        while (posIt.hasNext()) {
            Map.Entry<Integer, Position> entry = posIt.next();
            if (entry.getKey() > index) {
                posIt.remove();
                toAdd.put(entry.getKey()+s.length(), entry.getValue());
            }
        }
        positions.putAll(toAdd);
        if(index > text.length()) {
            text += org.apache.commons.lang3.StringUtils.repeat(' ', index - text.length());
        }
        text = text.substring(0, index) + s + text.substring(index);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + CStringUtil.escapeC(getText().getBytes()) + ")";
    }
}
