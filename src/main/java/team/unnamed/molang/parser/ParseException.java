package team.unnamed.molang.parser;

import team.unnamed.molang.lexer.Cursor;

import java.io.IOException;

/**
 * Exception that can be thrown during the
 * parsing phase
 */
public class ParseException extends IOException {

    private final Cursor cursor;

    public ParseException(Cursor cursor) {
        this.cursor = cursor;
    }

    public ParseException(String message, Cursor cursor) {
        super(appendCursor(message, cursor));
        this.cursor = cursor;
    }

    public ParseException(Throwable cause, Cursor cursor) {
        super(cause);
        this.cursor = cursor;
    }

    public ParseException(String message, Throwable cause, Cursor cursor) {
        super(appendCursor(message, cursor), cause);
        this.cursor = cursor;
    }

    public Cursor getCursor() {
        return cursor;
    }

    private static String appendCursor(String message, Cursor cursor) {
        if (cursor == null) return message; // todo
        // default format for exception messages, i.e.
        // "unexpected token: '%'"
        // "    at line 2, column 6"
        return message + "\n\tat " + cursor.toString();
    }

}
