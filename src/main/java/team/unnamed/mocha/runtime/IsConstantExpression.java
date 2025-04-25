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
import org.jetbrains.annotations.Nullable;
import team.unnamed.mocha.parser.ast.*;
import team.unnamed.mocha.runtime.value.Function;
import team.unnamed.mocha.runtime.value.ObjectProperty;
import team.unnamed.mocha.runtime.value.ObjectValue;
import team.unnamed.mocha.runtime.value.Value;

/**
 * A {@link ExpressionVisitor} that determines whether an expression is
 * constant or not.
 *
 * @since 3.0.0
 */
public final class IsConstantExpression implements ExpressionVisitor<@NotNull Boolean> {
    private static final ExpressionVisitor<Boolean> INSTANCE = new IsConstantExpression(null);

    private final ExpressionInterpreter<?> evaluator;
    private final ObjectValue scope;

    private IsConstantExpression(final @Nullable Scope scope) {
        this.evaluator = scope == null ? null : new ExpressionInterpreter<>(null, scope);
        this.scope = scope;
    }

    public static boolean test(final @NotNull Expression expression) {
        return expression.visit(INSTANCE);
    }

    public static boolean test(final @NotNull Expression expression, final @NotNull Scope scope) {
        return expression.visit(new IsConstantExpression(scope));
    }

    @Override
    public @NotNull Boolean visitArrayAccess(final @NotNull ArrayAccessExpression expression) {
        // array access is constant if the array and the index are constants
        return expression.array().visit(this) && expression.index().visit(this);
    }

    @Override
    public @NotNull Boolean visitFloat(final @NotNull FloatExpression expression) {
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
        if (scope == null) {
            // scope not given, can't know if it's constant or not
            return false;
        }

        final ObjectProperty property = scope.getProperty(expression.name());
        if (property == null) {
            // property not found, can't know if it's constant or not
            return false;
        }

        // property is constant if it's constant (duh) (copilot wrote this)
        return property.constant();
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
        final Expression objectExpr = expression.object();

        if (!objectExpr.visit(this)) {
            // object is not constant, accessing won't be constant either
            return false;
        }

        if (evaluator == null) {
            // can't evaluate the value of our object, can't know if accessing a
            // property will be constant or not
            return false;
        }

        final Value objectValue = objectExpr.visit(evaluator);
        if (!(objectValue instanceof ObjectValue)) {
            // it's not an object, accessing it will always give zero, so it is constant
            return true;
        }

        final ObjectProperty property = ((ObjectValue) objectValue).getProperty(expression.property());
        if (property == null) {
            // property not found, can't know if it's constant or not
            return false;
        }
        return property.constant();
    }

    @Override
    public @NotNull Boolean visitCall(final @NotNull CallExpression expression) {
        for (final Expression argument : expression.arguments()) {
            if (!argument.visit(this)) {
                // non-constant argument indicates non-constant call
                return false;
            }
        }

        final Expression functionExpr = expression.function();

        // check for built-in functions
        if (functionExpr instanceof IdentifierExpression) {
            final String name = ((IdentifierExpression) functionExpr).name();
            if (name.equalsIgnoreCase("loop") || name.equalsIgnoreCase("for_each")) {
                return true;
            }
        }

        if (!functionExpr.visit(this)) {
            // function is not constant (reference to this function may variate,
            //   this doesn't indicate if the function is pure/inlineable)
            return false;
        }

        if (evaluator == null) {
            // can't evaluate the value of our function, can't know if calling it will
            // be constant or not
            return false;
        }

        final Value function = functionExpr.visit(evaluator);
        if (!(function instanceof Function<?>)) {
            // trying to call this function (which is not a function) will always result
            // in an error (zero), which is constant
            return true;
        }

        // function call is constant if function is pure
        return ((Function<?>) function).pure();
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
