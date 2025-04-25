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
package team.unnamed.mocha.runtime.value;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ValueOfTest {
    @Test
    void test() {
        assertEquals(NumberValue.of(5), Value.of(5));
        assertEquals(StringValue.of("hello"), Value.of("hello"));
        assertEquals(NumberValue.of(1), Value.of(true));
        assertEquals(NumberValue.zero(), Value.of(false));
        assertEquals(NumberValue.zero(), Value.of(null));
        assertEquals(NumberValue.zero(), Value.of(new Object()));
        assertEquals(ArrayValue.of(), Value.of(new Object[0]));
        assertEquals(ArrayValue.of(), Value.of(new int[0]));
        assertEquals(ArrayValue.of(), Value.of(new float[0]));
        assertEquals(ArrayValue.of(NumberValue.of(1)), Value.of(new int[]{1}));
        assertEquals(ArrayValue.of(NumberValue.of(1)), Value.of(new float[]{1}));
        assertEquals(ArrayValue.of(NumberValue.of(1)), Value.of(new Object[]{1}));
        assertEquals(ArrayValue.of(NumberValue.of(1), NumberValue.of(2)), Value.of(new int[]{1, 2}));
        assertEquals(ArrayValue.of(NumberValue.of(1), NumberValue.of(2)), Value.of(new float[]{1, 2}));
        assertEquals(ArrayValue.of(NumberValue.of(1), StringValue.of("hello"), NumberValue.zero()), Value.of(new Object[]{1, "hello", false}));
    }
}
