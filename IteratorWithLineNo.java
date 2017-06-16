import java.util.Iterator;

class IteratorWithLineNo implements Iterator<Character> {
    final Iterator<Character> itr;
    int lineNo = 1;

    IteratorWithLineNo(Iterator<Character> itr) {
        this.itr = itr;
    }

    public int lineNo() {
        return lineNo;
    }

    public boolean hasNext() {
        return itr.hasNext();
    }

    public Character next() {
        Character c = itr.next();
        if (c == '\n') ++lineNo;
        return c;
    }
}
