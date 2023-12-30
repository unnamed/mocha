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

import org.jetbrains.annotations.NotNull;
import team.unnamed.mocha.parser.MolangParser;
import team.unnamed.mocha.parser.ParseException;
import team.unnamed.mocha.parser.ast.Expression;
import team.unnamed.mocha.runtime.ExpressionInterpreter;
import team.unnamed.mocha.runtime.MochaFunction;
import team.unnamed.mocha.runtime.MolangCompiler;
import team.unnamed.mocha.runtime.Scope;
import team.unnamed.mocha.runtime.compiled.MochaCompiledFunction;
import team.unnamed.mocha.runtime.value.MutableObjectBinding;
import team.unnamed.mocha.runtime.value.NumberValue;
import team.unnamed.mocha.runtime.value.Value;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

final class MochaEngineImpl<T> implements MochaEngine<T> {
    private final Scope scope;
    private final T entity;
    private final MolangCompiler compiler;

    public MochaEngineImpl(final T entity, final Consumer<Scope.Builder> scopeBuilder) {
        Scope.Builder builder = Scope.builder();
        scopeBuilder.accept(builder);
        this.scope = builder.build();
        this.entity = entity;
        this.compiler = new MolangCompiler(getClass().getClassLoader(), scope);
    }


    @Override
    public double eval(final @NotNull List<Expression> expressions) {
        // create bindings that just apply for this evaluation
        final Scope localBindings = scope.copy();
        //localBindings.setAllFrom(this.bindings);
        {
            // create temp bindings
            MutableObjectBinding temp = new MutableObjectBinding();
            localBindings.forceSet("temp", temp);
            localBindings.forceSet("t", temp);
        }
        ExpressionInterpreter<T> evaluator = new ExpressionInterpreter<>(entity, localBindings);
        Value lastResult = NumberValue.zero();

        for (Expression expression : expressions) {
            lastResult = expression.visit(evaluator);
            Value returnValue = evaluator.popReturnValue();
            if (returnValue != null) {
                lastResult = returnValue;
                break;
            }
        }

        // ensure returned value is a number
        return lastResult == null ? 0D : lastResult.getAsNumber();
    }

    @Override
    public double eval(final @NotNull Reader source) {
        final List<Expression> parsed;
        try {
            parsed = parse(source);
        } catch (final ParseException e) {
            // parse errors just output zero
            return 0;
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to read from given reader", e);
        }
        return eval(parsed);
    }

    @Override
    public @NotNull MochaFunction prepareEval(final @NotNull Reader reader) {
        final List<Expression> parsed;
        try {
            parsed = parse(reader);
        } catch (final ParseException e) {
            // parse errors just output zero
            return () -> 0D;
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to read from given reader", e);
        }
        return new MochaFunction() {
            @Override
            public double evaluate() {
                return eval(parsed);
            }

            @Override
            public @NotNull String toString() {
                return "MochaPreparedFunction(" + parsed + ")";
            }
        };
    }

    @Override
    public @NotNull MochaFunction prepareEval(final @NotNull String code) {
        final List<Expression> parsed;
        try {
            parsed = parse(code);
        } catch (final ParseException e) {
            // parse errors just output zero
            return new MochaFunction() {
                @Override
                public double evaluate() {
                    return 0D;
                }

                @Override
                public @NotNull String toString() {
                    return "MochaPreparedFunction('" + code + "', " + e.getMessage() + ")";
                }
            };
        }

        return new MochaFunction() {
            @Override
            public double evaluate() {
                return eval(parsed);
            }

            @Override
            public @NotNull String toString() {
                return "MochaPreparedFunction(" + parsed + ")";
            }
        };
    }

    @Override
    public <F extends MochaCompiledFunction> @NotNull F compile(final @NotNull Reader reader, final @NotNull Class<F> interfaceType) {
        List<Expression> parsed;
        try {
            parsed = parse(reader);
        } catch (final ParseException e) {
            parsed = Collections.emptyList();
        } catch (final IOException e) {
            throw new RuntimeException("Failed to read from given reader", e);
        }
        return compiler.compile(parsed, interfaceType);
    }

    @Override
    public @NotNull Scope scope() {
        return scope;
    }

    @Override
    public void bindVariable(String key, Object binding) {
    }

    @Override
    public void bindQuery(String key, Object binding) {
    }

    @Override
    public @NotNull List<Expression> parse(final @NotNull Reader reader) throws IOException {
        return MolangParser.parser(reader).parseAll();
    }

}
