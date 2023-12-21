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
package team.unnamed.molang;

import org.jetbrains.annotations.NotNull;
import team.unnamed.molang.parser.MolangParser;
import team.unnamed.molang.parser.ParseException;
import team.unnamed.molang.parser.ast.Expression;
import team.unnamed.molang.runtime.ExpressionEvaluator;
import team.unnamed.molang.runtime.binding.ObjectBinding;
import team.unnamed.molang.runtime.binding.ValueConversions;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

final class MolangEngineImpl<T> implements MolangEngine<T> {
    private final T entity;
    private final ObjectBinding bindings;

    public MolangEngineImpl(final T entity) {
        this.entity = entity;
        bindings = new ObjectBinding();
        final ObjectBinding variableBindings = new ObjectBinding();
        bindings.setProperty("variable", variableBindings);
        bindings.setProperty("v", variableBindings);
    }


    @Override
    public double eval(List<Expression> expressions) {
        // create bindings that just apply for this evaluation
        final ObjectBinding localBindings = new ObjectBinding();
        localBindings.setAllFrom(this.bindings);
        {
            // create temp bindings
            ObjectBinding temp = new ObjectBinding();
            localBindings.setProperty("temp", temp);
            localBindings.setProperty("t", temp);
        }
        ExpressionEvaluator<T> evaluator = ExpressionEvaluator.evaluator(entity, localBindings);
        Object lastResult = 0;

        for (Expression expression : expressions) {
            lastResult = expression.visit(evaluator);
            Object returnValue = evaluator.popReturnValue();
            if (returnValue != null) {
                lastResult = returnValue;
                break;
            }
        }

        // ensure returned value is a number
        return ValueConversions.asDouble(lastResult);
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
    public @NotNull ObjectBinding bindings() {
        return bindings;
    }

    @Override
    public void bindVariable(String key, Object binding) {
        final ObjectBinding variableBinding = (ObjectBinding) bindings.getProperty("variable");
        variableBinding.setProperty(key, binding);
    }

    @Override
    public void bindQuery(String key, Object binding) {
        final ObjectBinding queryBinding = (ObjectBinding) bindings.getProperty("query");
        queryBinding.setProperty(key, binding);
    }

    @Override
    public List<Expression> parse(Reader reader) throws IOException {
        return MolangParser.parser(reader).parseAll();
    }

}
