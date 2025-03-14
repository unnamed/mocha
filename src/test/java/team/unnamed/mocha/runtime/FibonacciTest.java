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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import team.unnamed.mocha.MochaEngine;
import team.unnamed.mocha.runtime.value.Function;
import team.unnamed.mocha.runtime.value.NumberValue;
import team.unnamed.mocha.runtime.value.ObjectProperty;
import team.unnamed.mocha.runtime.value.ObjectValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.StringJoiner;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FibonacciTest {

    @Test
    @DisplayName("Test executing the fibonacci.molang code")
    public void test() throws IOException {
        final String code = "v.x = 0;\n" +
                "v.y = 1;\n" +
                "loop(10, {\n" +
                "    query.log(v.x);\n" +
                "    t.x = v.x + v.y;\n" +
                "    v.x = v.y;\n" +
                "    v.y = t.x;\n" +
                "});\n" +
                "return v.y;";

        // generate the expected output
        // (we do this like this to ensure it's equal on every system,
        //  since they may have different line separators)
        String expected;
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(out);
            ps.println("0.0");
            ps.println("1.0");
            ps.println("1.0");
            ps.println("2.0");
            ps.println("3.0");
            ps.println("5.0");
            ps.println("8.0");
            ps.println("13.0");
            ps.println("21.0");
            ps.println("34.0");
            expected = out.toString();
        }


        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream stdout = new PrintStream(out);

        MochaEngine<?> engine = MochaEngine.createStandard();
        engine.scope().set("query", (ObjectValue) name -> {
            if (name.equalsIgnoreCase("log")) {
                return ObjectProperty.property((Function<?>) (ctx, args) -> {
                    int i = 0;
                    final StringJoiner joiner = new StringJoiner(" ");
                    while (i++ < args.length()) {
                        joiner.add(args.next().eval().getAsString());
                    }
                    stdout.println(joiner);
                    return NumberValue.zero();
                }, true);
            }
            return null;
        });
        final double result = engine.eval(code);

        assertEquals(expected, out.toString());
        assertEquals(89D, result);
    }

}
