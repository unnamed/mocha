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
package team.unnamed.mocha.runtime.jvm;

import org.junit.jupiter.api.Test;

class MolangCompilerTest {
    @MolangNative("math.abs")
    public static double abs(double a) {
        return Math.abs(a);
    }

    @Test
    void test() {
        MolangCompiler compiler = MolangCompiler.compiler();
        ScriptType script = compiler.compile("false ? a : b", ScriptType.class);
        System.out.println(script.eval(1, 2));
    }

    @Test
    void test_native() {
        final MolangCompiler compiler = MolangCompiler.compiler();
        compiler.registerStaticNatives(MolangCompilerTest.class);
        System.out.println(compiler.compile("3 * math.abs(5 * 5 * -1) + 1").evaluate());
    }

    interface ScriptType extends MolangFunction {
        int eval(@Named("a") double a, @Named("b") double b);
    }
}
