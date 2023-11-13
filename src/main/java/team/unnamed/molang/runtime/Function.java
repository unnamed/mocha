package team.unnamed.molang.runtime;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.molang.parser.ast.Expression;
import team.unnamed.molang.runtime.binding.ValueConversions;

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
public interface Function {
    /**
     * Executes this function with the given arguments.
     *
     * @param context   The execution context
     * @param arguments The arguments
     * @return The function result
     * @since 3.0.0
     */
    @Nullable Object evaluate(final @NotNull ExecutionContext<?> context, final @NotNull Argument @NotNull ... arguments);

    /**
     * Represents a {@link Function} argument. It is an expression
     * that can be easily evaluated inside the function.
     *
     * @since 3.0.0
     */
    interface Argument {
        /**
         * Gets the argument expression.
         *
         * @return The argument expression.
         * @since 3.0.0
         */
        @NotNull Expression expression();

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
    }
}
