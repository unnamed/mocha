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

final class RequiresContextVisitor implements ExpressionVisitor<Boolean> {
    private static final ExpressionVisitor<Boolean> INSTANCE = new RequiresContextVisitor();

    private RequiresContextVisitor() {
    }

    public static boolean test(final @NotNull Expression expression) {
        return expression.visit(INSTANCE);
    }

    @Override
    public Boolean visitDouble(final @NotNull DoubleExpression expression) {
        return false;
    }

    @Override
    public Boolean visitString(final @NotNull StringExpression expression) {
        return false;
    }

    @Override
    public Boolean visitIdentifier(final @NotNull IdentifierExpression expression) {
        return true;
    }

    @Override
    public Boolean visitTernaryConditional(final @NotNull TernaryConditionalExpression expression) {
        return expression.condition().visit(this)
                || expression.trueExpression().visit(this)
                || expression.falseExpression().visit(this);
    }

    @Override
    public Boolean visitUnary(final @NotNull UnaryExpression expression) {
        return expression.expression().visit(this);
    }

    @Override
    public Boolean visitExecutionScope(final @NotNull ExecutionScopeExpression expression) {
        for (final Expression expr : expression.expressions()) {
            if (expr.visit(this)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Boolean visitBinary(final @NotNull BinaryExpression expression) {
        return expression.left().visit(this) || expression.right().visit(this);
    }

    @Override
    public Boolean visitAccess(final @NotNull AccessExpression expression) {
        return expression.object().visit(this); // 'string'.length may not require context
    }

    @Override
    public Boolean visitCall(final @NotNull CallExpression expression) {
        return true;
    }

    @Override
    public Boolean visitStatement(final @NotNull StatementExpression expression) {
        return false;
    }

    @Override
    public Boolean visit(final @NotNull Expression expression) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
