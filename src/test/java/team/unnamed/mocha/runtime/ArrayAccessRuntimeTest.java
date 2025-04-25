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
package team.unnamed.mocha.runtime;

import org.junit.jupiter.api.Test;
import team.unnamed.mocha.MochaEngine;
import team.unnamed.mocha.runtime.binding.Binding;

import java.util.function.UnaryOperator;

import static team.unnamed.mocha.MochaAssertions.assertEvaluates;

class ArrayAccessRuntimeTest {
    @Test
    void test() {
        final UnaryOperator<MochaEngine<?>> configurer = engine -> {
            engine.bind(QueryImpl.class);
            return engine;
        };

        assertEvaluates(5F, "query.values[0]", configurer);
        assertEvaluates(10F, "query.values[1]", configurer);
        assertEvaluates(100F, "query.values[2]", configurer);
        assertEvaluates(100F, "q.values[20 + 3]", configurer);
        assertEvaluates(5F, "query.values[-1]", configurer);
        assertEvaluates(5F, "q.values[-1000]", configurer);
        assertEvaluates(5F, "q.values[0.5]", configurer);
        assertEvaluates(10F, "q.values[0.5 + 0.5]", configurer);
        assertEvaluates(100F, "q.values[math.pi - 1]", configurer);
    }

    @Binding({"query", "q"})
    public static class QueryImpl {
        @Binding("values")
        public static final double[] VALUES = {5D, 10D, 100D};
    }
}
