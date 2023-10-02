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

import java.util.Objects;

/**
 * Implementation of MoLang 1.17 ternary conditional expression,
 * it's similar to an "if {...} else {...}" expression.
 *
 * See https://bedrock.dev/docs/1.17.0.0/1.17.30.4/Molang#Conditionals
 *
 * Depending on the conditional expression, it may
 * return the {@code trueExpression} or the {@code falseExpression}
 */
public class TernaryConditionalExpression implements Expression {

    private final Expression conditional;
    private final Expression trueExpression;
    private final Expression falseExpression;

    public TernaryConditionalExpression(
            Expression conditional,
            Expression trueExpression,
            Expression falseExpression
    ) {
        this.conditional = conditional;
        this.trueExpression = trueExpression;
        this.falseExpression = falseExpression;
    }

    /**
     * Returns the condition of this ternary conditional expression, if this
     * condition is evaluated to {@code true}, the {@code trueExpression} is
     * evaluated and returned as value when evaluated
     */
    public Expression condition() {
        return conditional;
    }

    /**
     * Returns the expression evaluated when the {@code conditional} expression
     * is evaluated to {@code true}
     */
    public Expression trueExpression() {
        return trueExpression;
    }

    /**
     * Returns the expression evaluated when the {@code conditional} expression
     * is evaluated to {@code false}
     */
    public Expression falseExpression() {
        return falseExpression;
    }

    @Override
    public <R> R visit(ExpressionVisitor<R> visitor) {
        return visitor.visitTernaryConditional(this);
    }

    @Override
    public String toSource() {
        return conditional.toSource()
                + " ? " + trueExpression.toSource()
                + " : " + falseExpression.toSource();
    }

    @Override
    public String toString() {
        return "TernaryCondition(" + conditional + ", "
                + trueExpression + ", "
                + falseExpression + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TernaryConditionalExpression that = (TernaryConditionalExpression) o;
        return conditional.equals(that.conditional)
                && trueExpression.equals(that.trueExpression)
                && falseExpression.equals(that.falseExpression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(conditional, trueExpression, falseExpression);
    }

}
