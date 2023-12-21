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
package team.unnamed.molang.runtime;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.molang.parser.ast.Expression;
import team.unnamed.molang.parser.ast.ExpressionVisitor;
import team.unnamed.molang.runtime.binding.ObjectBinding;

/**
 * An {@link ExpressionVisitor} implementation that evaluates
 * (interprets) the expressions it visits and returns a single
 * value, commonly, a double value.
 *
 * @since 3.0.0
 */
public /* sealed */ interface ExpressionEvaluator<T> /* permits ExpressionEvaluatorImpl */ extends ExecutionContext<T>, ExpressionVisitor<Object> {
    /**
     * Creates a new {@link ExpressionEvaluator} instance with
     * the given bindings.
     *
     * @param entity   The entity object
     * @param bindings The bindings to use.
     * @return The created expression evaluator.
     * @since 3.0.0
     */
    static <T> @NotNull ExpressionEvaluator<T> evaluator(final @Nullable T entity, final @NotNull ObjectBinding bindings) {
        return new ExpressionEvaluatorImpl<>(entity, bindings);
    }

    /**
     * Creates a new {@link ExpressionEvaluator} instance with
     * the given bindings.
     *
     * @param bindings The bindings to use.
     * @return The created expression evaluator.
     * @since 3.0.0
     */
    static <T> @NotNull ExpressionEvaluator<T> evaluator(final @NotNull ObjectBinding bindings) {
        return evaluator(null, bindings);
    }

    /**
     * Creates a new {@link ExpressionEvaluator} instance
     * without bindings.
     *
     * @return The created expression evaluator.
     * @since 3.0.0
     */
    static <T> @NotNull ExpressionEvaluator<T> evaluator() {
        return evaluator(ObjectBinding.EMPTY);
    }

    @Override
    default @Nullable Object eval(final @NotNull Expression expression) {
        return expression.visit(this);
    }

    /**
     * Gets the bindings for this evaluator.
     *
     * @return The evaluator bindings.
     * @since 3.0.0
     */
    @NotNull ObjectBinding bindings();

    /**
     * Creates a new, child, expression evaluator.
     *
     * <p>Child evaluators have all the bindings of
     * their parents and may have extra bindings.</p>
     *
     * <p>Child evaluators have their own stack.</p>
     *
     * @return The child expression evaluator.
     * @since 3.0.0
     */
    @NotNull ExpressionEvaluator<T> createChild();

    /**
     * Creates a new, child, expression evaluator.
     *
     * <p>Child evaluators have all the bindings of
     * their parents and may have extra bindings.</p>
     *
     * <p>Child evaluators have their own stack.</p>
     *
     * @param entity The new entity value
     * @return The child expression evaluator.
     * @since 3.0.0
     */
    <R> ExpressionEvaluator<R> createChild(final R entity);

    /**
     * Pops the return value, set by the last "return"
     * expression.
     *
     * @return The return value, null if no "return"
     * expression is found.
     * @since 3.0.0
     */
    @Nullable Object popReturnValue();

}