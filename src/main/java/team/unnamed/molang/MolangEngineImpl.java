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

import team.unnamed.molang.parser.MolangParser;
import team.unnamed.molang.parser.ast.Expression;
import team.unnamed.molang.runtime.ExpressionEvaluator;
import team.unnamed.molang.runtime.binding.ObjectBinding;
import team.unnamed.molang.runtime.binding.StandardBindings;

import javax.script.ScriptException;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

final class MolangEngineImpl implements MolangEngine {

    private final ObjectBinding bindings;

    MolangEngineImpl(MolangEngine.Builder builder) {
        this.bindings = builder.bindings;
    }

    private ObjectBinding createBindings() {
        ObjectBinding bindings = new ObjectBinding();
        bindings.setAllFrom(StandardBindings.BUILT_IN);
        bindings.setAllFrom(this.bindings);
        ObjectBinding temp = new ObjectBinding();
        bindings.setProperty("temp", temp);
        bindings.setProperty("t", temp);
        return bindings;
    }

    @Override
    public Object eval(List<Expression> expressions) {
        ObjectBinding bindings = createBindings();
        ExpressionEvaluator evaluator = ExpressionEvaluator.evaluator(bindings);
        Object lastResult = 0;

        for (Expression expression : expressions) {
            lastResult = expression.visit(evaluator);
            Object returnValue = evaluator.popReturnValue();
            if (returnValue != null) {
                lastResult = returnValue;
                break;
            }
        }

        return lastResult;
    }

    @Override
    public Object eval(Reader reader) throws ScriptException {
        try {
            return eval(parse(reader));
        } catch (IOException e) {
            throw new ScriptException(e);
        }
    }

    @Override
    public List<Expression> parse(Reader reader) throws IOException {
        return MolangParser.parser(reader).parseAll();
    }

}
