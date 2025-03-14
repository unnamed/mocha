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
package team.unnamed.mocha.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CaseInsensitiveStringHashMapTest {
    @Test
    void test() {
        final Map<String, String> map = new CaseInsensitiveStringHashMap<>();
        map.put("Hello", "World");
        map.put("Hello2", "World2");

        assertEquals(2, map.size());

        // key set should be lowercase
        assertEquals(new HashSet<>(Arrays.asList("hello", "hello2")), map.keySet());

        assertEquals("World", map.get("Hello"));
        assertEquals("World", map.get("hello"));
        assertEquals("World", map.get("HELLO"));
        assertEquals("World2", map.get("Hello2"));
        assertEquals("World2", map.get("hello2"));
        assertEquals("World2", map.get("hElLo2"));

        // should replace
        map.put("hello", "World3");

        assertEquals(2, map.size());
        assertEquals("World3", map.get("Hello"));
        assertEquals("World3", map.get("hello"));
        assertEquals("World3", map.get("HELLO"));

        // key set should remain the same, everything lowercase
        assertEquals(new HashSet<>(Arrays.asList("hello", "hello2")), map.keySet());

        // Set should also be case-insensitive
        assertEquals(2, map.keySet().size());
        assertTrue(map.keySet().contains("hello"));
        assertTrue(map.keySet().contains("HELLO"));
        assertTrue(map.keySet().contains("HeLLo"));
        assertTrue(map.keySet().contains("hELLO2"));
        assertTrue(map.keySet().contains("hello2"));

        // removing on keySet should also take effect on Map
        map.keySet().remove("HELlO");
        assertEquals(1, map.keySet().size());
        assertEquals(1, map.size());
        assertFalse(map.containsKey("hello"));
        assertNull(map.get("hElLo"));
        assertTrue(map.containsKey("hello2"));
        assertEquals("World2", map.get("hello2"));
    }
}
