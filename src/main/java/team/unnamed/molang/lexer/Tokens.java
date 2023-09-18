package team.unnamed.molang.lexer;

/**
 * Utility class holding utility static
 * methods for working with character
 * tokens
 */
public final class Tokens {

    /**
     * Character used to escape other characters
     * to consider them special characters or
     * non-tokens
     */
    public static final char ESCAPE = '\\';
    public static final char UNDERSCORE = '_';
    public static final char DOT = '.';

    // MoLang currently only supports single quotes for string
    public static final char QUOTE = '\'';

    private Tokens() {
    }

    public static boolean isLetter(int c) {
        return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z');
    }

    public static boolean isDigit(int c) {
        return Character.isDigit(c);
    }

    public static boolean isValidForWord(int c) {
        return isLetter(c) || c == UNDERSCORE;
    }

    public static boolean isValidForWordContinuation(int c) {
        return isValidForWord(c) || isDigit(c);
    }

}
