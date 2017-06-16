class CsvParseException extends Exception {
    CsvParseException(String message, int lineNo) {
        super (message + ", line " + lineNo);
    }
}
