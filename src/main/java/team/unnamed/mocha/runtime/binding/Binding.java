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
package team.unnamed.mocha.runtime.binding;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Gives a name to a type (class, interface, enum), method or field
 * to be used from Molang scripts.
 *
 * <p>Binding names are concatenated with dots (.) to access
 * nested types, methods or fields.</p>
 *
 * <p>For example, if you have a class named {@code Foo} with a
 * field named {@code bar} and a method named {@code baz}, you
 * can bind them as follows:</p>
 *
 * <pre>
 * {@literal @}Binding("foo")
 *  class Foo {
 *     {@literal @}Binding("baz")
 *      public static int baz = 0;
 *
 *     {@literal @}Binding("bar")
 *      public static void bar() {}
 *  }
 * </pre>
 *
 * <p>Then, you can access them from Molang scripts as follows:</p>
 *
 * <pre>
 *     foo.baz
 *     foo.bar()
 * </pre>
 *
 * <p>Note that for non-static bindings, the instance name will be used
 * instead.</p>
 *
 * <p>A class annotated with {@link Binding} can also implement</p>
 *
 * @since 3.0.0
 */
@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Binding {
    /**
     * Returns the name for this binding.
     *
     * @return The name for this binding.
     * @since 3.0.0
     */
    @NotNull String value();

    /**
     * Returns whether this binding should be skipped
     * from the binding checking/normalizing process.
     *
     * <p>For example, functions that return a double
     * value must be checked in order to replace NaN and
     * Infinity values by zero. If a function already
     * does that, then it can be skipped from the automatic
     * process.</p>
     *
     * @return Whether this binding should be skipped
     * from the binding checking/normalizing process.
     * @since 3.0.0
     */
    boolean skipChecking() default false;

    /**
     * (For methods only) Determines if this method is pure or not.
     *
     * <p>A pure method is a method that has the following properties:</p>
     * <ol>
     *     <li>The method return values are <b>identical for identical
     *     arguments</b>, and</li>
     *     <li>The method has <b>no side effects</b></li>
     * </ol>
     *
     * <p>The compiler or interpreter may pre-evaluate these functions ahead
     * of time. In case of compiling, the function may be called during compile
     * time, to use the function's result instead.</p>
     *
     * @return If this function is pure or not.
     * @since 3.0.0
     */
    boolean pure() default false;
}
