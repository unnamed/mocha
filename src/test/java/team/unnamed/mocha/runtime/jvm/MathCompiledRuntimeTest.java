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
package team.unnamed.mocha.runtime.jvm;

import org.junit.jupiter.api.Test;
import team.unnamed.mocha.MochaEngine;
import team.unnamed.mocha.runtime.compiled.MochaCompiledFunction;
import team.unnamed.mocha.runtime.compiled.Named;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MathCompiledRuntimeTest {
    @Test
    void test() {
        final MochaEngine<?> engine = MochaEngine.createStandard();
        {
            // 0: dload_1
            // 1: invokestatic
            // 4: dreturn
            final MathFunction cos = engine.compile("math.cos(x)", MathFunction.class);
            assertEquals(1F, cos.apply(0), 0.0001);
            assertEquals(4F / 5D, cos.apply(37), 0.01);
            assertEquals(3F / 5D, cos.apply(53), 0.01);
            assertEquals(0F, cos.apply(90), 0.0001);
            assertEquals(-1F, cos.apply(180), 0.0001);
        }
        {
            // 0: dload_1
            // 1: invokestatic
            // 4: l2d
            // 5: dreturn
            final MathFunction round = engine.compile("math.round(x)", MathFunction.class);
            assertEquals(5.0F, round.apply(5.4F));
            assertEquals(6.0F, round.apply(5.5F));
            assertEquals(6.0F, round.apply(5.6F));
            assertEquals(-5.0F, round.apply(-5.4F));
        }
    }

    public interface MathFunction extends MochaCompiledFunction {
        float apply(@Named("x") float x);
    }
}
