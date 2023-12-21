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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import team.unnamed.molang.parser.ParseException;
import team.unnamed.molang.parser.ast.Expression;

import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Compares the results of this library with the
 * results of other libraries
 */
public class CompareTest {

    private static final MolangEngine<?> ENGINE = MolangEngine.createDefault();

    //#region Helper code
    private static BufferedReader createResourceReader(String name) {
        InputStream stream = CompareTest.class
                .getClassLoader()
                .getResourceAsStream(name);
        if (stream == null) {
            throw new IllegalStateException("Resource not found: " + name);
        }
        return new BufferedReader(new InputStreamReader(stream));
    }

    private static String nextNonEmpty(BufferedReader reader) throws IOException {
        String value;
        do {
            value = reader.readLine();
            if (value == null) {
                break;
            } else {
                value = value.trim();
            }
        } while (value.isEmpty() || value.charAt(0) == '#');
        return value;
    }

    private static void compare(String expectationsName, String sourceName) throws IOException {
        try (BufferedReader source = createResourceReader(sourceName)) {
            try (BufferedReader expectations = createResourceReader(expectationsName)) {
                while (true) {
                    String expression = nextNonEmpty(source);
                    String expected = nextNonEmpty(expectations);

                    if (expression == null || expected == null) {
                        // end reached
                        break;
                    }

                    float expectedValue = Float.parseFloat(expected);

                    // eval expression
                    try {
                        List<Expression> expressions = ENGINE.parse(expression);
                        Object result = ENGINE.eval(expressions);
                        Assertions.assertTrue(result instanceof Number, "Result is a number");
                        Assertions.assertEquals(
                                expectedValue,
                                ((Number) result).floatValue(),
                                () -> "Incorrect result. Parsed syntax tree:\n\t" + expressions.stream()
                                        .map(Expression::toString)
                                        .collect(Collectors.joining(";\n\t")) + ";\n"
                                        + "For expression: " + expression
                        );
                    } catch (ParseException e) {
                        Assertions.fail("Failed to parse expression: " + expression, e);
                    } catch (ScriptException e) {
                        Assertions.fail("Failed to eval expression '" + expression + "'", e);
                    }
                }
            }
        }
    }

    /**
     * Compares this library results with MolangJS
     * of JannisX11
     * https://github.com/JannisX11/MolangJS
     */
    @Test
    public void compare_with_molangjs() throws IOException {
        compare("expectations.txt", "tests.txt");
    }
    //#endregion

}
