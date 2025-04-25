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

import static team.unnamed.mocha.MochaAssertions.assertEvaluatesAndCompiles;

class CaseSensitivityTest {
    @Test
    void test() {
        assertEvaluatesAndCompiles(Math.PI, "MATH.PI");
        assertEvaluatesAndCompiles(Math.PI, "math.PI");
        assertEvaluatesAndCompiles(Math.PI, "Math.PI");
        assertEvaluatesAndCompiles(Math.PI, "Math.pi");
        assertEvaluatesAndCompiles(Math.PI, "math.pi");
        assertEvaluatesAndCompiles(Math.PI, "MATH.pi");
        assertEvaluatesAndCompiles(Math.PI, "MATH.Pi");

        assertEvaluatesAndCompiles(20F, "MATH.ABS(-20)");
        assertEvaluatesAndCompiles(20F, "math.ABS(-20)");
        assertEvaluatesAndCompiles(20F, "Math.ABS(-20)");
        assertEvaluatesAndCompiles(20F, "Math.abs(-20)");

        assertEvaluatesAndCompiles(50F, "Math.clamp(10, 50, 100)");
        assertEvaluatesAndCompiles(50F, "Math.CLAMP(10, 50, 100)");
        assertEvaluatesAndCompiles(50F, "math.clamp(10, 50, 100)");
        assertEvaluatesAndCompiles(50F, "math.CLAMP(10, 50, 100)");

        assertEvaluatesAndCompiles(108.1415, "MATH.ABS(-100) + Math.sqrt(25) + math.PI");

        assertEvaluatesAndCompiles(1F, "tEMP.x = 1; return Temp.X;");
        assertEvaluatesAndCompiles(1F, "temp.X = 2; tEMp.y = 3; return TEMP.Y - Temp.x;");
    }
}
