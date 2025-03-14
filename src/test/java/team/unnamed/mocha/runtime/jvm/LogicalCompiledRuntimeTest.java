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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogicalCompiledRuntimeTest {
    @Test
    void test() {
        final MochaEngine<?> engine = MochaEngine.createStandard();

        {
            // 0: iload_1
            // 1: ifeq        +11 (12)
            // 4: iload_2
            // 5: ifeq        +7 (12)
            // 8: iconst_1
            // 9: goto        +4 (13)
            // 12: iconst_0
            // 13: ireturn
            final LogicalFunction and = engine.compile("a && b", LogicalFunction.class);
            assertTrue(and.apply(true, true));
            assertFalse(and.apply(true, false));
            assertFalse(and.apply(false, true));
            assertFalse(and.apply(false, false));
        }

        {
            // 0: iload_1
            // 1: ifne        +7 (8)
            // 4: iload_2
            // 5: ifeq        +7 (12)
            // 8: iconst_1
            // 9: goto        +4 (13)
            // 12: iconst_0
            // 13: ireturn
            final LogicalFunction or = engine.compile("a || b", LogicalFunction.class);
            assertTrue(or.apply(true, true));
            assertTrue(or.apply(true, false));
            assertTrue(or.apply(false, true));
            assertFalse(or.apply(false, false));
        }
    }

    public interface LogicalFunction extends MochaCompiledFunction {
        boolean apply(@Named("a") boolean a, @Named("b") boolean b);
    }
}
