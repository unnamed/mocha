package team.unnamed.molang;

import org.junit.jupiter.api.Assertions;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class MoLangAssertions {

    private static final ScriptEngine ENGINE = new MoLangScriptEngineFactory().getScriptEngine();

    public static void assertEquals(float expected, String... expressions) {
        String expression = String.join(";", expressions);
        try {
            Object result = ENGINE.eval(expression);
            Assertions.assertTrue(result instanceof Number, "Result is a number");
            Assertions.assertEquals(expected, ((Number) result).floatValue());
        } catch (ScriptException e) {
            Assertions.fail("Failed to eval expression: " + expression, e);
        }
    }

}
