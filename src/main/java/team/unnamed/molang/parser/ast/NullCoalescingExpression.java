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

/**
 * The null coalescing expression implementation,
 * if the result of evaluating the 'leftHand' expression
 * is considered invalid, then it returns the 'rightHand'
 * result
 */
public final class NullCoalescingExpression implements Expression {

    private final Expression value;
    private final Expression fallback;

    public NullCoalescingExpression(Expression value, Expression fallback) {
        this.value = value;
        this.fallback = fallback;
    }

    public Expression value() {
        return value;
    }

    public Expression fallback() {
        return fallback;
    }

    @Override
    public <R> R visit(ExpressionVisitor<R> visitor) {
        return visitor.visitNullCoalescing(this);
    }

    @Override
    public String toSource() {
        return value.toSource() + " ?? "
                + fallback.toSource();
    }

    @Override
    public String toString() {
        return "NullCoalescing(" + value + ", " + fallback + ")";
    }

}
