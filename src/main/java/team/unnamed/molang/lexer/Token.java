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
