/*
 * This file is part of mocha, licensed under the MIT license
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
package team.unnamed.mocha.runtime;

import org.jetbrains.annotations.NotNull;
import team.unnamed.mocha.parser.ast.*;

/**
 * A {@link ExpressionVisitor} that determines whether an expression is
 * constant or not.
 *
 * @since 3.0.0
 */
public final class IsConstantExpression implements ExpressionVisitor<@NotNull Boolean> {
    private static final ExpressionVisitor<Boolean> INSTANCE = new IsConstantExpression();

    private IsConstantExpression() {
    }

    public static boolean test(final @NotNull Expression expression) {
        return expression.visit(INSTANCE);
    }

    @Override
    public @NotNull Boolean visitArrayAccess(final @NotNull ArrayAccessExpression expression) {
        // array access is constant if the array and the index are constants
        return expression.array().visit(this) && expression.index().visit(this);
    }

    @Override
    public @NotNull Boolean visitDouble(final @NotNull DoubleExpression expression) {
        // literals are constants
        return true;
    }

    @Override
    public @NotNull Boolean visitString(final @NotNull StringExpression expression) {
        // literals are constants
        return true;
    }

    @Override
    public @NotNull Boolean visitIdentifier(final @NotNull IdentifierExpression expression) {
        // identifiers are not constants
        return false;
    }

    @Override
    public @NotNull Boolean visitTernaryConditional(final @NotNull TernaryConditionalExpression expression) {
        // ternary conditional is only constant if all of its parts are constant
        return expression.condition().visit(this)
                && expression.trueExpression().visit(this)
                && expression.falseExpression().visit(this);
    }

    @Override
    public @NotNull Boolean visitUnary(final @NotNull UnaryExpression expression) {
        // unary expressions are constant if their expression is constant
        return expression.expression().visit(this);
    }

    @Override
    public @NotNull Boolean visitExecutionScope(final @NotNull ExecutionScopeExpression expression) {
        // execution scopes are constant if all of their expressions are constant
        for (final Expression expr : expression.expressions()) {
            if (!expr.visit(this)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public @NotNull Boolean visitBinary(final @NotNull BinaryExpression expression) {
        // binary expressions are constant if both of their expressions are constant
        return expression.left().visit(this) && expression.right().visit(this);
    }

    @Override
    public @NotNull Boolean visitAccess(final @NotNull AccessExpression expression) {
        // access expressions are constant if their object is constant
        return expression.object().visit(this); // 'string'.length may not require context
    }

    @Override
    public @NotNull Boolean visitCall(final @NotNull CallExpression expression) {
        // calls are never constant
        // todo: they may be, e.g. math.min(1, 2) is always 1
        return false;
    }

    @Override
    public @NotNull Boolean visitStatement(final @NotNull StatementExpression expression) {
        // statements are constants
        return true;
    }

    @Override
    public @NotNull Boolean visit(final @NotNull Expression expression) {
        // can't know if they are constant or not
        return false;
    }
}
