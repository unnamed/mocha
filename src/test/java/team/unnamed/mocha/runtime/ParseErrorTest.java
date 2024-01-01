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

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import team.unnamed.mocha.MochaEngine;
import team.unnamed.mocha.runtime.binding.Binding;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParseErrorTest {
    @Test
    void test() {
        final MochaEngine<?> engine = MochaEngine.createStandard();
        engine.bind(QueryImpl.class);

        engine.handleParseExceptions(e -> assertEquals("Found error token: Unexpected token '\"', expected single quote (') to start a string literal\n\tat line 1, column 11", e.getMessage()));

        // should error since strings only allow single quotes
        engine.eval("query.log(\"Hello world!\");");
    }

    @Binding({"query", "q"})
    public static final class QueryImpl {
        private static final List<String> LOGS = new ArrayList<>();

        @Binding("log")
        public static void log(final @NotNull String str) {
            LOGS.add(str);
        }

        @Binding("flush")
        public static void flush() {
            LOGS.forEach(System.out::println);
        }
    }
}
