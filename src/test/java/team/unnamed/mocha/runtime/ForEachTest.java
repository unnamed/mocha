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
package team.unnamed.mocha.runtime;

import org.junit.jupiter.api.Test;
import team.unnamed.mocha.MochaEngine;
import team.unnamed.mocha.runtime.binding.Binding;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ForEachTest {
    @Test
    void test() {
        final String code = String.join("\n",
                "v.sum = 0;",
                "for_each(v.age, query.list_ages(), {",
                "    v.sum = v.sum + v.age;",
                "});",
                "return v.sum;");

        final MochaEngine<?> engine = MochaEngine.createStandard();
        engine.bind(QueryImpl.class);

        final double result = engine.eval(code);
        assertEquals(98, result);
    }

    @Binding({"query", "q"})
    public static final class QueryImpl {
        @Binding("list_ages")
        public static double[] listAges() {
            return new double[]{18D, 16D, 40D, 24D};
        }
    }
}
