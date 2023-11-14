package team.unnamed.molang.runtime.jvm;

import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.io.StringReader;

public interface MolangCompiler {
    /**
     * Creates a new Molang compiler instance.
     *
     * @return The new Molang compiler instance.
     * @since 3.0.0
     */
    static @NotNull MolangCompiler compiler() {
        return new MolangCompilerImpl(MolangCompiler.class.getClassLoader());
    }

    /**
     * Registers the natives from the given class. Will
     * check for all the static methods in this class
     * annotated with {@link MolangNative} and will make
     * them accessible from the compiled scripts.
     *
     * @param clazz The class to register natives from.
     * @since 3.0.0
     */
    void registerStaticNatives(final @NotNull Class<?> clazz);

    /**
     * Registers the natives from the given object. Will
     * check for all the methods in this object
     * annotated with {@link MolangNative} and will make
     * them accessible from the compiled scripts.
     *
     * @param object The object to register natives from.
     * @since 3.0.0
     */
    void registerNatives(final @NotNull Object object);

    /**
     * Compiles the given source code into a Molang function
     * of the given class.
     *
     * @param reader The source reader.
     * @param clazz  The class of the function.
     * @param <T>    The type of the function.
     * @return The compiled function.
     * @since 3.0.0
     */
    <T extends MolangFunction> @NotNull T compile(final @NotNull Reader reader, final @NotNull Class<T> clazz);

    /**
     * Compiles the given script into a Molang function
     * of the given class.
     *
     * @param script The script to compile.
     * @param clazz  The class of the function.
     * @param <T>    The type of the function.
     * @return The compiled function.
     * @since 3.0.0
     */
    default <T extends MolangFunction> @NotNull T compile(final @NotNull String script, final @NotNull Class<T> clazz) {
        return compile(new StringReader(script), clazz);
    }

    /**
     * Compiles the given script into a Molang function
     * with no arguments.
     *
     * @param script The script to compile.
     * @return The compiled function.
     * @since 3.0.0
     */
    default @NotNull MolangNullaryFunction compile(final @NotNull String script) {
        return compile(script, MolangNullaryFunction.class);
    }
}
