package team.unnamed.molang.runtime.jvm;

/**
 * Marker interface for Molang compiled functions.
 *
 * <p>This interface is supposed to be extended by
 * user-defined interfaces with a single method, that
 * they will be able to call to directly execute the
 * function.</p>
 *
 * <p>See the following example on compiling a function
 * that computes how a player's level is computed from
 * their experience:</p>
 * <pre>{@code
 * interface PlayerLevelFunction extends MolangFunction {
 *     int computeLevel(@Named("xp") int experience);
 * }
 *
 * // ...
 * MolangCompiler compiler = ...;
 * PlayerLevelFunction function = compiler.compile("100 * sqrt(xp)", PlayerLevelFunction.class);
 *
 * // cache "function" and then...
 * function.computeLevel(100); // 31
 * }</pre>
 *
 * <p>Note that all the parameters from the function method <b>must</b>
 * be annotated with {@link Named} to know what is their name in the
 * script.</p>
 *
 * @see MolangCompiler
 * @see Named
 * @since 3.0.0
 */
public interface MolangFunction {
}
