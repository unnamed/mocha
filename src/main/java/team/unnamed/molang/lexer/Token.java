package team.unnamed.molang.lexer;

/**
 * Class representing a Molang token. Each token has some
 * information set by the lexer (i.e. start/end position
 * and token kind)
 *
 * @since 1.0.0
 */
public final class Token {

    private final TokenKind kind;
    private final /* @Nullable */ String value;
    private final int start;
    private final int end;

    public Token(
            TokenKind kind,
            /* @Nullable */ String value,
            int start,
            int end
    ) {
        this.kind = kind;
        this.value = value;
        this.start = start;
        this.end = end;

        // verify
        if (kind.hasTag(TokenKind.Tag.HAS_VALUE) && value == null) {
            throw new IllegalArgumentException("A token with kind "
                    + kind + " must have a non-null value");
        }
    }

    public TokenKind kind() {
        return kind;
    }

    public /* @Nullable */ String value() {
        return value;
    }

    public int start() {
        return start;
    }

    public int end() {
        return end;
    }

    @Override
    public String toString() {
        if (kind.hasTag(TokenKind.Tag.HAS_VALUE)) {
            return kind + "(" + value + ")";
        } else {
            return kind.toString();
        }
    }

}
