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
import org.jetbrains.annotations.Nullable;
import team.unnamed.mocha.parser.ast.Expression;
import team.unnamed.mocha.runtime.binding.ValueConversions;

/**
 * Represents a Molang function. Receives a certain amount of
 * parameters and (optionally) returns a value. Can be called
 * from Molang code using call expressions: {@code my_function(1, 2, 3)}
 *
 * <p>This is a very low-level function that is "expression-sensitive",
 * this means, it takes the raw expression arguments instead of the
 * evaluated expression argument values.</p>
 *
 * @since 3.0.0
 */
@FunctionalInterface
public interface Function<T> {
    /**
     * Executes this function with the given arguments.
     *
     * @param context   The execution context
     * @param arguments The arguments
     * @return The function result
     * @since 3.0.0
     */
    @Nullable Object evaluate(final @NotNull ExecutionContext<T> context, final @NotNull Arguments arguments);

    /**
     * Executes this function.
     *
     * @param context The execution context
     * @return The function result
     * @since 3.0.0
     */
    default @Nullable Object evaluate(final @NotNull ExecutionContext<T> context) {
        return evaluate(context, Arguments.empty());
    }

    /**
     * Represents a collection of {@link Function} {@link Argument}s.
     *
     * @since 3.0.0
     */
    interface Arguments {
        static @NotNull Arguments empty() {
            return ExpressionEvaluatorImpl.FunctionArguments.EMPTY;
        }

        /**
         * Gets the next argument. If there are
         * no more arguments, returns an {@link Argument}
         * with no expression and that can be evaluated
         * to null-like values (0 for numbers).
         *
         * @return The next argument.
         */
        @NotNull Argument next();

        /**
         * Gets the amount of arguments.
         *
         * @return The amount of arguments.
         * @since 3.0.0
         */
        int length();
    }

    /**
     * Represents a {@link Function} argument. It is an expression
     * that can be easily evaluated inside the function.
     *
     * @since 3.0.0
     */
    interface Argument {
        /**
         * Gets the argument expression. Null if and only if
         * the argument wasn't actually provided and is just
         * returned by {@link Arguments} for ease of use.
         *
         * @return The argument expression.
         * @since 3.0.0
         */
        @Nullable Expression expression();

        /**
         * Evaluates the argument expression.
         *
         * @return The evaluation result.
         * @since 3.0.0
         */
        @Nullable Object eval();

        /**
         * Evaluates the argument expression as a double.
         *
         * @return The evaluation result as a double.
         * @since 3.0.0
         */
        default double evalAsDouble() {
            return ValueConversions.asDouble(eval());
        }

        /**
         * Evaluates the argument expression as a string.
         *
         * @return The evaluation result as a string.
         * @since 3.0.0
         */
        default @Nullable String evalAsString() {
            return ValueConversions.asString(eval());
        }
    }
}
