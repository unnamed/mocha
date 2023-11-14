package team.unnamed.molang.runtime.jvm;

/**
 * A {@link MolangFunction} that can be evaluated without arguments.
 *
 * @since 3.0.0
 */
@FunctionalInterface
public interface MolangNullaryFunction extends MolangFunction {
    /**
     * Evaluates this function.
     *
     * @return The result of the evaluation.
     * @since 3.0.0
     */
    double evaluate();
}
