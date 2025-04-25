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

import static org.junit.jupiter.api.Assertions.*;

class ArithmeticCompiledRuntimeTest {
    @Test
    void test() {
        final MochaEngine<?> engine = MochaEngine.createStandard();

        {
            // 0: dload_1
            // 1: dload_3
            // 2: dcmpl
            // 3: ifgt          10
            // 6: iconst_0
            // 7: goto          11
            // 10: iconst_1
            // 11: ireturn
            final ComparisonFunction gt = engine.compile("a > b", ComparisonFunction.class);
            assertTrue(gt.compare(10, 5));
            assertFalse(gt.compare(-50, -20));
            assertFalse(gt.compare(3F, 3F));
        }

        {
            // 0: dload_1
            // 1: dload_3
            // 2: dcmpl
            // 3: iflt           10
            // 6: iconst_0
            // 7: goto           11
            // 10: iconst_1
            // 11: ireturn
            final ComparisonFunction lt = engine.compile("a < b", ComparisonFunction.class);
            assertFalse(lt.compare(10, 5));
            assertTrue(lt.compare(-50, -20));
            assertFalse(lt.compare(3F, 3F));
        }

        {
            // 0: dload_1
            // 1: dload_3
            // 2: dcmpl
            // 3: ifge         10
            // 6: iconst_0
            // 7: goto         11
            // 10: iconst_1
            // 11: ireturn
            final ComparisonFunction gte = engine.compile("a >= b", ComparisonFunction.class);
            assertTrue(gte.compare(10, 5));
            assertFalse(gte.compare(-50, -20));
            assertTrue(gte.compare(3F, 3F));
        }

        {
            // 0: dload_1
            // 1: dload_3
            // 2: dcmpl
            // 3: ifle         10
            // 6: iconst_0
            // 7: goto         11
            // 10: iconst_1
            // 11: ireturn
            final ComparisonFunction lte = engine.compile("a <= b", ComparisonFunction.class);
            assertFalse(lte.compare(10, 5));
            assertTrue(lte.compare(-50, -20));
            assertTrue(lte.compare(3F, 3F));
        }

        {
            // 0: dload_1
            // 1: dload_3
            // 2: dcmpl
            // 3: ifne          10
            // 6: iconst_0
            // 7: goto          11
            // 10: iconst_1
            // 11: ireturn
            final ComparisonFunction eq = engine.compile("a == b", ComparisonFunction.class);
            assertFalse(eq.compare(10, 5));
            assertFalse(eq.compare(-50, -20));
            assertTrue(eq.compare(3F, 3F));
        }

        {
            // 0: dload_1
            // 1: dload_3
            // 2: dcmpl
            // 3: ifeq          10
            // 6: iconst_0
            // 7: goto          11
            // 10: iconst_1
            // 11: ireturn
            final ComparisonFunction neq = engine.compile("a != b", ComparisonFunction.class);
            assertTrue(neq.compare(10, 5));
            assertTrue(neq.compare(-50, -20));
            assertFalse(neq.compare(3F, 3F));
        }

        // requiring casting
        {
            // 0: dload_1
            // 1: dload_3
            // 2: dcmpl
            // 3: ifgt        10
            // 6: lconst_0
            // 7: goto        11
            // 10: lconst_1
            // 11: lreturn
            final StupidLongComparisonFunction gt = engine.compile("a > b", StupidLongComparisonFunction.class);
            assertEquals(1L, gt.compare(10, 5));
            assertEquals(0L, gt.compare(-50, -20));
            assertEquals(0L, gt.compare(3F, 3F));
        }
    }

    public interface ComparisonFunction extends MochaCompiledFunction {
        boolean compare(@Named("a") float a, @Named("b") float b);
    }

    // like why would someone need long?
    public interface StupidLongComparisonFunction extends MochaCompiledFunction {
        long compare(@Named("a") float a, @Named("b") float b);
    }
}
