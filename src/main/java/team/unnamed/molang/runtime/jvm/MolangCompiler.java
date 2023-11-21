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
     * @param name   The object's name in the script.
     * @since 3.0.0
     */
    void registerNatives(final @NotNull Object object, final @NotNull String name);

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
