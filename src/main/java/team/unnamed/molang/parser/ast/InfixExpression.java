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

import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

/**
 * Represents any binary expression that operates two
 * expressions, they can be arithmetic or boolean
 */
public final class InfixExpression implements Expression {

    public enum Op {
        AND,
        OR,
        LT,
        LTE,
        GT,
        GTE,
        ADD,
        SUB,
        MUL,
        DIV
    }

    public static final int AND = 0;
    public static final int OR = 1;
    public static final int LESS_THAN_OR_EQUAL = 3;
    public static final int GREATER_THAN_OR_EQUAL = 5;

    private final Op op;
    private final Expression left;
    private final Expression right;

    public InfixExpression(
            final @NotNull Op op,
            final @NotNull Expression left,
            final @NotNull Expression right
    ) {
        this.op = requireNonNull(op, "op");
        this.left = requireNonNull(left, "left");
        this.right = requireNonNull(right, "right");
    }

    public @NotNull Op op() {
        return op;
    }

    public Expression left() {
        return left;
    }

    public Expression right() {
        return right;
    }

    @Override
    public <R> R visit(@NotNull ExpressionVisitor<R> visitor) {
        return visitor.visitInfix(this);
    }

    @Override
    public String toString() {
        return op.name() + "(" + left + ", " + right + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InfixExpression that = (InfixExpression) o;
        if (op != that.op) return false;
        if (!left.equals(that.left)) return false;
        return right.equals(that.right);
    }

    @Override
    public int hashCode() {
        int result = op.hashCode();
        result = 31 * result + left.hashCode();
        result = 31 * result + right.hashCode();
        return result;
    }

}