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
package team.unnamed.mocha;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import team.unnamed.mocha.lexer.Cursor;
import team.unnamed.mocha.parser.ParseException;
import team.unnamed.mocha.parser.ast.Expression;
import team.unnamed.mocha.runtime.GlobalScope;
import team.unnamed.mocha.runtime.MochaFunction;
import team.unnamed.mocha.runtime.binding.JavaObjectBinding;
import team.unnamed.mocha.runtime.compiled.MochaCompiledFunction;
import team.unnamed.mocha.runtime.standard.MochaMath;
import team.unnamed.mocha.runtime.value.Function;
import team.unnamed.mocha.runtime.value.MutableObjectBinding;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

/**
 * The engine's entry class. Provides methods to evaluate
 * and parse Molang code from strings and readers.
 *
 * @since 3.0.0
 */
public interface MochaEngine<T> {
    static <T> MochaEngine<T> create(T entity) {
        return new MochaEngineImpl<>(entity);
    }

    static MochaEngine<?> create() {
        return new MochaEngineImpl<>(null);
    }

    /**
     * Creates a new, clean and empty {@link MochaEngine} instance
     * with the standard, default bindings.
     *
     * @return The created {@link MochaEngine} instance.
     * @since 3.0.0
     */
    @Contract("-> new")
    static @NotNull MochaEngine<?> createStandard() {
        final MochaEngine<?> engine = create();
        engine.bindDefaults();
        return engine;
    }

    /**
     * Parses the data from the given {@code reader}
     * to a {@link List} of {@link Expression}
     *
     * <strong>Note that this method won't close
     * the given {@code reader}</strong>
     *
     * @throws ParseException If read failed or there
     *                        are syntax errors in the script
     */
    List<Expression> parse(Reader reader) throws IOException;

    /**
     * Parses the given {@code string} to a list of
     * {@link Expression}
     *
     * @param string The MoLang string
     * @return The list of parsed expressions
     * @throws ParseException If parsing fails
     */
    default List<Expression> parse(String string) throws ParseException {
        try (Reader reader = new StringReader(string)) {
            return parse(reader);
        } catch (ParseException e) {
            throw e;
        } catch (IOException e) {
            throw new ParseException("Failed to close string reader", e, new Cursor(0, 0));
        }
    }

    double eval(List<Expression> expressions);

    double eval(Reader reader);

    default double eval(String script) {
        try (StringReader reader = new StringReader(script)) {
            return eval(reader);
        }
    }

    //#region COMPILING API

    /**
     * Compiles the given code into a Molang function
     * that can take arguments.
     *
     * @param reader        The code to compile.
     * @param interfaceType The interface to implement, must
     *                      have a single method.
     * @return The compiled function.
     * @since 3.0.0
     */
    <F extends MochaCompiledFunction> @NotNull F compile(final @NotNull Reader reader, final @NotNull Class<F> interfaceType);

    /**
     * Compiles the given code into a Molang function
     * that can take arguments.
     *
     * @param code          The code to compile.
     * @param interfaceType The interface to implement, must
     *                      have a single method.
     * @return The compiled function.
     * @since 3.0.0
     */
    default <F extends MochaCompiledFunction> @NotNull F compile(final @NotNull String code, final @NotNull Class<F> interfaceType) {
        try (final StringReader reader = new StringReader(code)) {
            return compile(reader, interfaceType);
        }
    }

    /**
     * Compiles the given code into a Molang function
     * that takes no arguments.
     *
     * @param reader The code to compile.
     * @return The compiled function.
     * @since 3.0.0
     */
    default @NotNull MochaFunction compile(final @NotNull Reader reader) {
        return compile(reader, MochaFunction.class);
    }

    /**
     * Compiles the given code into a Molang function
     * that takes no arguments.
     *
     * @param code The code to compile.
     * @return The compiled function.
     * @since 3.0.0
     */
    default @NotNull MochaFunction compile(final @NotNull String code) {
        try (final StringReader reader = new StringReader(code)) {
            return compile(reader);
        }
    }

    //#endregion END COMPILING API

    /**
     * Returns the bindings for this Molang engine
     * instance.
     *
     * @return This engine's bindings
     * @since 3.0.0
     */
    @NotNull GlobalScope scope();

    /**
     * Makes this engine use the default bindings.
     *
     * <p>Default bindings include math and query bindings.</p>
     *
     * @since 3.0.0
     */
    default void bindDefaults() {
        scope().forceSet("math", JavaObjectBinding.of(MochaMath.class, new MochaMath()));

        final MutableObjectBinding variableBinding = new MutableObjectBinding();
        scope().forceSet("variable", variableBinding);
        scope().forceSet("v", variableBinding);
    }

    void bindVariable(String key, Object binding);

    void bindQuery(String key, Object binding);

    default void bindQueryFunction(String key, Function<T> function) {
        bindQuery(key, function);
    }
}
