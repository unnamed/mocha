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
}
