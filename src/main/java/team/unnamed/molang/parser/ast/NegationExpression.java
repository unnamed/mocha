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
 * Expression implementation for the MoLang 1.17
 * negation expression, it may negate numbers or
 * boolean expressions
 */
public class NegationExpression implements Expression {

    private final Expression expression;
    private final char token;

    public NegationExpression(Expression expression, char token) {
        this.expression = expression;
        this.token = token;
    }

    /**
     * Returns the negated expression
     * @return The negated expression,
     * never null
     */
    public Expression expression() {
        return expression;
    }

    @Override
    public <R> R visit(ExpressionVisitor<R> visitor) {
        return visitor.visitNegation(this);
    }

    @Override
    public String toSource() {
        return token + expression.toSource();
    }

    @Override
    public String toString() {
        return "Negation(" + expression + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NegationExpression that = (NegationExpression) o;
        if (token != that.token) return false;
        return expression.equals(that.expression);
    }

    @Override
    public int hashCode() {
        int result = expression.hashCode();
        result = 31 * result + (int) token;
        return result;
    }
}
