import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.NoSuchElementException;

public class JavaCsv {
    private StringBuilder buf;
    private List<String> result;

    interface State {
        State onChar(char c, int lineNo) throws Exception;
    }

    class InitState implements State {
        public State onChar(char c, int lineNo) throws Exception {
            switch (c) {
            case '\0':
            case '\n':
                result.add(buf.toString());
                return endState;
            case '\r':
                return initState;
            case '"':
                if (buf.length() == 0) return inQuoteState;
                else throw new CsvParseException
                     ("Parse error. Should be enclosed by double quote if data contains double quotes.",
                      lineNo);
            case ',':
                result.add(buf.toString());
                buf.setLength(0);
                return initState;
            default:
                buf.append(c);
                return initState;
            }
        }
    }

    class InQuoteState implements State {
        public State onChar(char c, int lineNo) throws Exception {
            switch (c) {
            case 0:
                throw new CsvParseException("Parse error. Quote is not closed.", lineNo);
            case '"':
                return inQuoteQuoteState;
            default:
                buf.append(c);
                return inQuoteState;
            }
        }
    }

    class InQuoteQuoteState implements State {
        public State onChar(char c, int lineNo) throws Exception {
            switch (c) {
            case 0:
                result.add(buf.toString());
                return endState;
            case ',':
                result.add(buf.toString());
                buf.setLength(0);
                return initState;
            case '\r':
                return inQuoteQuoteState;
            case '\n':
                result.add(buf.toString());
                return endState;
            case '"':
                buf.append(c);
                return inQuoteState;
            default:
                throw new CsvParseException("Parse error. Invalid character '" + c + "' after quote.", lineNo);
            }
        }
    }

    class EndState implements State {
        public State onChar(char c, int lineNo) throws Exception {
            throw new RuntimeException("End state does not accept input.");
        }
    }

    final InitState initState = new InitState();
    final InQuoteState inQuoteState = new InQuoteState();
    final InQuoteQuoteState inQuoteQuoteState = new InQuoteQuoteState();
    final EndState endState = new EndState();

    public List<String> parseLine(IteratorWithLineNo itr) throws Exception {
        buf = new StringBuilder();
        result = new ArrayList<>();

        State current = initState;
        while (itr.hasNext()) {
            char c = itr.next();
            current = current.onChar(c, itr.lineNo());
            if (current == endState) return result;
        }
        current.onChar((char)0, itr.lineNo());
        return result;
    }

    public List<String> parseLine(Iterator<Character> itr) throws Exception {
        return parseLine(new IteratorWithLineNo(itr));
    }

    public static void main(String... args) throws Exception {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; ++i) {
            parse();
        }

        System.err.printf("Elapsed %,d%n", System.currentTimeMillis() - start);
    }

    static void parse() throws Exception {
        final String text = "1,\"2\",3\n5,6,7";
        Iterator<Character> it = new Iterator<Character>() {
            int idx = 0;
            public boolean hasNext() {
                return idx < text.length();
            }
            public Character next() {
                if (! hasNext()) throw new NoSuchElementException();
                return text.charAt(idx++);
            }
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };

        while (it.hasNext()) {
            new JavaCsv().parseLine(new IteratorWithLineNo(it));
        }
    }
}
