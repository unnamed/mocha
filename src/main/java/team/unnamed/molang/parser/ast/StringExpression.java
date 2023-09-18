package team.unnamed.molang.parser.ast;

import team.unnamed.molang.lexer.Tokens;

/**
 * Literal expression implementation for MoLang 1.17
 * strings
 */
public final class StringExpression implements Expression {

    private final String value;

    public StringExpression(String value) {
        this.value = value;
    }

    /**
     * Returns the value for this string
     * expression, never null
     */
    public String value() {
        return value;
    }

    @Override
    public <R> R visit(ExpressionVisitor<R> visitor) {
        return visitor.visitString(this);
    }

    @Override
    public String toSource() {
        return Tokens.QUOTE + escapeQuotes(value) + Tokens.QUOTE;
    }

    @Override
    public String toString() {
        return "String('" + escapeQuotes(value) + "')";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringExpression that = (StringExpression) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    /**
     * Escapes quotes ({@link Tokens#QUOTE}) in the given
     * {@code value} using {@link Tokens#ESCAPE}
     *
     * <strong>Currently not required, but done, 1.17.30.4
     * specification declares that escape characters aren't
     * supported</strong>
     *
     * @param value The string value to process
     * @return The processed string
     */
    public static String escapeQuotes(String value) {
        int length = value.length();
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char c = value.charAt(i);
            if (c == Tokens.QUOTE) {
                builder.append(Tokens.ESCAPE);
            }
            builder.append(c);
        }
        return builder.toString();
    }

}
