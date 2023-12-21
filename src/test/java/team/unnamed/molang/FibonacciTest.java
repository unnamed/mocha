/*
 * This file is part of molang, licensed under the MIT license
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
package team.unnamed.molang;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import team.unnamed.molang.runtime.binding.StandardBindings;

import javax.script.ScriptException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FibonacciTest {

    @Test
    @DisplayName("Test executing the fibonacci.molang code")
    public void test() throws IOException {

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

        MolangEngine engine = MolangEngine.create();
        engine.bindDefaults();
        engine.bindings().setProperty("query", StandardBindings.createQueryBinding(() -> stdout));
        Object result;

        try (Reader reader = new InputStreamReader(FibonacciTest.class.getClassLoader().getResourceAsStream("fibonacci.molang"))) {
            result = engine.eval(reader);
        } catch (ScriptException e) {
            throw new IOException("Evaluation failed", e);
        }

        // now check the output
        assertEquals(
                expected,
                out.toString()
        );
        assertTrue(result instanceof Number);
        assertEquals(
                89.0F,
                ((Number) result).floatValue()
        );
    }

}
