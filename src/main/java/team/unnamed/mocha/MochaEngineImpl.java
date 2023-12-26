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
import team.unnamed.mocha.runtime.ExpressionEvaluator;
import team.unnamed.mocha.runtime.GlobalScope;
import team.unnamed.mocha.runtime.MochaFunction;
import team.unnamed.mocha.runtime.MolangCompiler;
import team.unnamed.mocha.runtime.value.MutableObjectBinding;
import team.unnamed.mocha.runtime.value.NumberValue;
import team.unnamed.mocha.runtime.value.Value;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

final class MochaEngineImpl<T> implements MochaEngine<T> {
    private final GlobalScope scope = GlobalScope.create();
    private final T entity;
    private final MolangCompiler compiler;

    public MochaEngineImpl(final T entity) {
        this.entity = entity;
        this.compiler = new MolangCompiler(getClass().getClassLoader(), scope);
    }


    @Override
    public double eval(List<Expression> expressions) {
        // create bindings that just apply for this evaluation
        final GlobalScope localBindings = scope.copy();
        //localBindings.setAllFrom(this.bindings);
        {
            // create temp bindings
            MutableObjectBinding temp = new MutableObjectBinding();
            localBindings.forceSet("temp", temp);
            localBindings.forceSet("t", temp);
        }
        ExpressionEvaluator<T> evaluator = ExpressionEvaluator.evaluator(entity, localBindings);
        Value lastResult = NumberValue.zero();

        for (Expression expression : expressions) {
            System.out.println(expression);
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
    public double eval(Reader reader) {
        final List<Expression> parsed;
        try {
            parsed = parse(reader);
        } catch (ParseException e) {
            return 0;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read from given reader", e);
        }
        return eval(parsed);
    }

    @Override
    public @NotNull MochaFunction compile(final @NotNull String script) {
        final List<Expression> parsed;
        try {
            parsed = parse(script);
        } catch (ParseException e) {
            return () -> 0;
        }
        return compiler.compile(parsed, MochaFunction.class);
    }

    @Override
    public @NotNull GlobalScope scope() {
        return scope;
    }

    @Override
    public void bindVariable(String key, Object binding) {
    }

    @Override
    public void bindQuery(String key, Object binding) {
    }

    @Override
    public List<Expression> parse(Reader reader) throws IOException {
        return MolangParser.parser(reader).parseAll();
    }

}
