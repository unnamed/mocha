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

public class MolangCompilerTest {
    @Test
    void test() {
        final MochaEngine<?> engine = MochaEngine.createStandard();
        final ScriptType script = engine.compile("false ? a : b", ScriptType.class);
        assertEquals(2, script.eval(1, 2));
        assertEquals(50, script.eval(20, 50));
        assertEquals(200, script.eval(50, 200));

        final ScriptType script2 = engine.compile("a + (b - a) * 0.5", ScriptType.class);
        assertEquals(5, script2.eval(1, 10));
        assertEquals(20, script2.eval(20, 20));
        assertEquals(50, script2.eval(-50, 150));

        final ScriptType script3 = engine.compile("(a > b) ? a : b", ScriptType.class);
        assertEquals(10, script3.eval(10, 5));
        assertEquals(50, script3.eval(50, -20));
        assertEquals(3, script3.eval(3F, 3F));

        final ScriptType script4 = engine.compile("(a < b) ? a : b", ScriptType.class);
        assertEquals(5, script4.eval(10, 5));
        assertEquals(-20, script4.eval(50, -20));
        assertEquals(3, script4.eval(3F, 3F));
    }

    @Test
    void test_native() {
        final MochaEngine<?> engine = MochaEngine.createStandard();
        //compiler.registerStaticNatives(MolangCompilerTest.class);
        System.out.println(engine.compile("3 * math.abs(5 * 5 * -1) + 1").evaluate());
    }

    public interface ScriptType extends MochaCompiledFunction {
        int eval(@Named("a") float a, @Named("b") float b);
    }
}
