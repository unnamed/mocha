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
import team.unnamed.mocha.parser.ParseException;
import team.unnamed.mocha.parser.ast.Expression;
import team.unnamed.mocha.runtime.MochaFunction;
import team.unnamed.mocha.runtime.Scope;
import team.unnamed.mocha.runtime.binding.Binding;
import team.unnamed.mocha.runtime.binding.JavaObjectBinding;
import team.unnamed.mocha.runtime.compiled.MochaCompiledFunction;
import team.unnamed.mocha.runtime.standard.MochaMath;
import team.unnamed.mocha.runtime.value.MutableObjectBinding;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * The engine's entry class. Provides methods to evaluate
 * and parse Molang code from strings and readers.
 *
 * @since 3.0.0
 */
public interface MochaEngine<T> {
    static <T> MochaEngine<T> create(T entity) {
        return new MochaEngineImpl<>(entity, b -> {
        });
    }

    static <T> MochaEngine<T> create(T entity, Consumer<Scope.Builder> scopeBuilder) {
        return new MochaEngineImpl<>(entity, scopeBuilder);
    }

    static MochaEngine<?> create() {
        return new MochaEngineImpl<>(null, b -> {
        });
    }

    /**
     * Creates a new, clean and empty {@link MochaEngine} instance
     * with the standard, default bindings.
     *
     * @return The created {@link MochaEngine} instance.
     * @since 3.0.0
     */
    @Contract("_ -> new")
    static <T> @NotNull MochaEngine<T> createStandard(T entity) {
        return create(entity, builder -> {
            builder.set("math", JavaObjectBinding.of(MochaMath.class, new MochaMath()));
            final MutableObjectBinding variableBinding = new MutableObjectBinding();
            builder.set("variable", variableBinding);
            builder.set("v", variableBinding);
        });
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
        return createStandard(null);
    }

    //#region PARSING API

    /**
     * Parses the data from the given {@code reader}
     * to a {@link List} of {@link Expression}
     *
     * <strong>Note that this method won't close
     * the given {@code reader}</strong>
     *
     * @param reader The reader to read the data from
     * @return The list of parsed expressions
     * @throws ParseException If read failed or there
     *                        are syntax errors in the script
     * @since 3.0.0
     */
    @NotNull List<Expression> parse(final @NotNull Reader reader) throws IOException;

    /**
     * Parses the given {@code string} to a list of
     * {@link Expression}
     *
     * @param string The MoLang string
     * @return The list of parsed expressions
     * @throws ParseException If parsing fails
     */
    default @NotNull List<Expression> parse(final @NotNull String string) throws ParseException {
        try (final StringReader reader = new StringReader(string)) {
            return parse(reader);
        } catch (final ParseException e) {
            throw e;
        } catch (final IOException e) {
            throw new UncheckedIOException("Error occurred reading the source code: '" + string + "'", e);
        }
    }

    //#endregion END PARSING API

    //#region INTERPRETER API

    /**
     * Evaluates the given {@code expressions}, these expressions
     * are already parsed and are interpreted as fast as possible.
     *
     * @param expressions The expressions to evaluate.
     * @return The result of the evaluation.
     * @since 3.0.0
     */
    double eval(final @NotNull List<Expression> expressions);

    /**
     * Parses and evaluates the given Molang source.
     *
     * <p>Note that the engine instance is not responsible
     * for caching parsed expressions, so if you want to
     * re-use parsed expressions, you should use the
     * {@link #parse(Reader)} and {@link #eval(List)}
     * methods.</p>
     *
     * @param source The source to evaluate.
     * @return The result of the evaluation.
     * @see #parse(Reader)
     * @see #eval(List)
     * @since 3.0.0
     */
    double eval(final @NotNull Reader source);

    /**
     * Parses and evaluates the given Molang source.
     *
     * <p>Note that the engine instance is not responsible
     * for caching parsed expressions, so if you want to
     * re-use parsed expressions, you should use the
     * {@link #parse(String)} and {@link #eval(List)}
     * methods.</p>
     *
     * @param source The source to evaluate.
     * @return The result of the evaluation.
     * @see #parse(String)
     * @see #eval(List)
     * @since 3.0.0
     */
    default double eval(final @NotNull String source) {
        requireNonNull(source, "script");
        try (final StringReader reader = new StringReader(source)) {
            return eval(reader);
        }
    }

    /**
     * Parses the data from the given {@code reader} and
     * returns a cached, interpretable {@link MochaFunction}.
     *
     * <pre><strong>Note that this method won't close the given
     * {@code reader}</strong></pre>
     *
     * <p>This approach is the same as parsing to a List of
     * expressions, caching them and then evaluating using
     * {@link #eval(List)}, but easier, since it already keeps
     * this {@link MochaEngine} instance.</p>
     *
     * @param reader The reader to read the data from
     * @return The cached, interpretable function
     * @since 3.0.0
     */
    @NotNull MochaFunction prepareEval(final @NotNull Reader reader);

    /**
     * Parses the given {@code string} and returns a cached,
     * interpretable {@link MochaFunction}.
     *
     * <p>This approach is the same as parsing to a List of
     * expressions, caching them and then evaluating using
     * {@link #eval(List)}, but easier, since it already keeps
     * this {@link MochaEngine} instance.</p>
     *
     * @param string The MoLang string
     * @return The cached, interpretable function
     * @since 3.0.0
     */
    default @NotNull MochaFunction prepareEval(final @NotNull String string) {
        try (final StringReader reader = new StringReader(string)) {
            return prepareEval(reader);
        }
    }
    //#endregion END INTERPRETER API

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
        requireNonNull(code, "code");
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
        requireNonNull(code, "code");
        try (final StringReader reader = new StringReader(code)) {
            return compile(reader);
        }
    }

    //#endregion END COMPILING API

    //#region BINDING API

    /**
     * Binds the given {@code clazz} static fields and methods.
     *
     * <p>Fields and methods are bound in the following format:</p>
     * <pre>
     *     namespace.field
     *     namespace.method()
     *     namespace.method(arg1, arg2)
     * </pre>
     *
     * <p>Where {@code namespace} is given by the given class'
     * {@link Binding} annotation, and the field and method
     * names are also given by {@link Binding} annotations.</p>
     *
     * @param clazz The class to bind.
     * @see Binding
     * @since 3.0.0
     */
    void bind(final @NotNull Class<?> clazz);
    //#endregion

    /**
     * Returns the bindings for this Molang engine
     * instance.
     *
     * @return This engine's bindings
     * @since 3.0.0
     */
    @NotNull Scope scope();
}
