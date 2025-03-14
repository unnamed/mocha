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
package team.unnamed.mocha.runtime.compiled;

import team.unnamed.mocha.runtime.MolangCompiler;

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
 * have a name, they can either be annotated with the {@link Named}
 * annotation or have a name in runtime (Compiler's -parameters flag)</p>
 *
 * <p>Also note that the function's returned value can take a null-like
 * value depending on the specified return type. For numbers, it will be
 * zero, and for objects it will be {@code null}</p>
 *
 * @see MolangCompiler
 * @see Named
 * @since 3.0.0
 */
public interface MochaCompiledFunction {
}
