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

    private static final MolangEngine ENGINE = MolangEngine.createDefault();

    /**
     * Compares this library results with MolangJS
     * of JannisX11
     * https://github.com/JannisX11/MolangJS
     */
    @Test
    public void compare_with_molangjs() throws IOException {
        compare("expectations.txt", "tests.txt");
    }

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
                                () -> "Incorrect result. Parsed expressions:\n" + expressions.stream()
                                        .map(Expression::toString)
                                        .collect(Collectors.joining(";\n")) + ";\n"
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
    //#endregion

}
