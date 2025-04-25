/*
 * This file is part of mocha, licensed under the MIT license
 *
 * Copyright (c) 2021-2025 Unnamed Team
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
package team.unnamed.mocha.runtime;

import org.jetbrains.annotations.NotNull;
import team.unnamed.mocha.parser.ast.BinaryExpression;
import team.unnamed.mocha.parser.ast.CallExpression;
import team.unnamed.mocha.parser.ast.FloatExpression;
import team.unnamed.mocha.parser.ast.Expression;
import team.unnamed.mocha.parser.ast.ExpressionVisitor;
import team.unnamed.mocha.parser.ast.TernaryConditionalExpression;

import static java.util.Objects.requireNonNull;

final class ExpressionInliner implements ExpressionVisitor<@NotNull Expression> {
    private final ExpressionInterpreter<?> interpreter;
    private final Scope scope;

    ExpressionInliner(final @NotNull ExpressionInterpreter<?> interpreter, final @NotNull Scope scope) {
        this.interpreter = requireNonNull(interpreter, "interpreter");
        this.scope = requireNonNull(scope, "scope");
    }

    @Override
    public @NotNull Expression visitBinary(final @NotNull BinaryExpression expression) {
        if (IsConstantExpression.test(expression, scope)) {
            // can be evaluated in compile-time
            return FloatExpression.of(expression.visit(interpreter).getAsNumber());
        }
        return expression;
    }

    @Override
    public @NotNull Expression visitTernaryConditional(final @NotNull TernaryConditionalExpression expression) {
        final Expression conditionExpr = expression.condition();
        final Expression trueExpr = expression.trueExpression();
        final Expression falseExpr = expression.falseExpression();

        if (IsConstantExpression.test(conditionExpr, scope)) {
            // condition can be evaluated in compile-time
            final boolean condition = conditionExpr.visit(interpreter).getAsBoolean();
            final Expression resultExpr = condition ? trueExpr : falseExpr;
            return resultExpr.visit(this);
        }

        return expression;
    }

    @Override
    public @NotNull Expression visitCall(final @NotNull CallExpression expression) {
        if (IsConstantExpression.test(expression, scope)) {
            // can be evaluated in compile-time
            return FloatExpression.of(expression.visit(interpreter).getAsNumber());
        }
        return expression;
    }

    @Override
    public @NotNull Expression visit(final @NotNull Expression expression) {
        return expression;
    }
}
