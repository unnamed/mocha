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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BreakContinueTest {

    @Test
    public void test_break() throws Exception {
        Object value = MochaEngine.createStandard().eval(
                "t.i = 0;" +
                        "loop(10, {" +
                        "t.i = t.i + 1;" +
                        "(t.i >= 5) ? break;" +
                        "});" +
                        "return t.i;"
        );
        assertTrue(value instanceof Number, "Value must be a number!");
        assertEquals(5.0D, ((Number) value).doubleValue());
    }

    @Test
    public void test_continue() throws Exception {
        Object value = MochaEngine.createStandard().eval(
                "t.i = 0;" +
                        "t.sum = 0;" +
                        "loop(20, {" +
                        "t.i = t.i + 1;" +
                        "((t.i < 8) || (t.i > 17)) ? continue;" +
                        "t.sum = t.sum + t.i;" +
                        "});" +
                        "return t.sum;"
        );
        assertTrue(value instanceof Number, "Value must be a number!");
        assertEquals(125.0D, ((Number) value).doubleValue());
    }

}
