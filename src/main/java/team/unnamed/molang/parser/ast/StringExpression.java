/*
 * This file is part of molang, licensed under the MIT license
 *
 * Copyright (c) 2021-2023 Unnamed Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package team.unnamed.molang.parser.ast;

import team.unnamed.molang.lexer.Characters;

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
        return Characters.QUOTE + escapeQuotes(value) + Characters.QUOTE;
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
     * Escapes quotes ({@link Characters#QUOTE}) in the given
     * {@code value} using {@link Characters#ESCAPE}
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
            if (c == Characters.QUOTE) {
                builder.append(Characters.ESCAPE);
            }
            builder.append(c);
        }
        return builder.toString();
    }

}
