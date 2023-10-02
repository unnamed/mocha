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
 * Implementation of MoLang 1.17 binary conditional
 * expression, it's similar to an "if {...} " expression.
 *
 * See https://bedrock.dev/docs/1.17.0.0/1.17.30.4/Molang#Conditionals
 *
 * <p>If the evaluated value of the {@code conditional}
 * expression is considered true, it evaluates the
 * {@code predicate} expression.</p>
 */
public class ConditionalExpression implements Expression {

    private final Expression condition;
    private final Expression predicate;

    public ConditionalExpression(Expression condition, Expression predicate) {
        this.condition = condition;
        this.predicate = predicate;
    }

    public Expression condition() {
        return condition;
    }

    public Expression predicate() {
        return predicate;
    }

    @Override
    public <R> R visit(ExpressionVisitor<R> visitor) {
        return visitor.visitConditional(this);
    }

    @Override
    public String toSource() {
        return condition.toSource() + " ? " + predicate.toSource();
    }

    @Override
    public String toString() {
        return "Condition(" + condition + ", " + predicate + ")";
    }

}
