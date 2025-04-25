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
package team.unnamed.mocha;

import org.jetbrains.annotations.NotNull;
import team.unnamed.mocha.parser.ParseException;
import team.unnamed.mocha.parser.ast.Expression;

import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public final class MochaAssertions {
    private MochaAssertions() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    public static void assertCreateSameTree(final @NotNull String expr1, final @NotNull String expr2) throws Exception {
        final MochaEngine<?> engine = MochaEngine.createStandard();
        final List<Expression> expressions1 = engine.parse(expr1);
        final List<Expression> expressions2 = engine.parse(expr2);
        assertEquals(expressions1, expressions2, () -> "Expressions:\n\t- " +
                expr1 +
                "\n\t- " +
                expr2 +
                "\nGenerated different syntax trees:\n\t- " +
                expressions1 +
                "\n\t- " +
                expressions2);
    }

    public static void assertCreateTree(final @NotNull String expr, final @NotNull Expression @NotNull ... expressions) {
        final MochaEngine<?> engine = MochaEngine.createStandard();
        final List<Expression> parsed;
        try {
            parsed = engine.parse(expr);
        } catch (final ParseException e) {
            fail("Failed to parse expression: '" + expr + "'", e);
            return;
        }
        assertEquals(
                Arrays.asList(expressions),
                parsed,
                () -> "Expression: '" + expr + "' generated unexpected syntax tree:\n\t" +
                        "- Expected: " + Arrays.toString(expressions) + "\n\t" +
                        "- Got: " + parsed
        );
    }

    public static void assertParseError(final @NotNull String expr, final int column) {
        final MochaEngine<?> engine = MochaEngine.createStandard();
        try {
            final List<Expression> expressions = engine.parse(expr);
            fail("Expected parse error at column " + column + " for expression: '" + expr + "', instead, it was parsed as: " + expressions);
        } catch (final ParseException e) {
            assertEquals(column, e.cursor().column(), "Expected parse error at column " + column + " for expression: '" + expr + "'");
        }
    }

    public static void assertEvaluates(final double expected, final @NotNull String expr, final @NotNull UnaryOperator<MochaEngine<?>> configurer) {
        MochaEngine<?> engine = MochaEngine.createStandard();
        engine = configurer.apply(engine);
        final double result = engine.eval(expr);
        assertEquals(expected, result, 0.0001, () -> "Expression: '" + expr + "' evaluated to " + result + ", expected " + expected);
    }

    public static void assertEvaluates(final double expected, final @NotNull String expr) {
        assertEvaluates(expected, expr, engine -> engine);
    }

    public static void assertEvaluatesAndCompiles(final double expected, final @NotNull String expr) {
        final MochaEngine<?> engine = MochaEngine.createStandard();
        final double result = engine.eval(expr);
        assertEquals(expected, result, 0.0001, () -> "(Interpreted) expression: '" + expr + "' evaluated to " + result + ", expected " + expected);

        final double compiledResult = engine.compile(expr).evaluate();
        assertEquals(expected, compiledResult, 0.0001, () -> "(Compiled) expression: '" + expr + "' evaluated to " + compiledResult + ", expected " + expected);
    }
}
