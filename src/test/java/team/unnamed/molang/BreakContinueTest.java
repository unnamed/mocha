package team.unnamed.molang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BreakContinueTest {

    @Test
    public void test_break() throws Exception {
        Object value = MolangEngine.createDefault().eval(
                "t.i = 0;" +
                "loop(10, {" +
                        "t.i = t.i + 1;" +
                        "(t.i >= 5) ? break;" +
                        "});" +
                "return t.i;"
        );
        assertTrue(value instanceof Number, "Value must be a number!");
        assertEquals(5.0D, ((Number) value).doubleValue());
    }

    @Test
    public void test_continue() throws Exception {
        Object value = MolangEngine.createDefault().eval(
                "t.i = 0;" +
                    "t.sum = 0;" +
                    "loop(20, {" +
                        "t.i = t.i + 1;" +
                        "((t.i < 8) || (t.i > 17)) ? continue;" +
                        "t.sum = t.sum + t.i;" +
                    "});" +
                    "return t.sum;"
        );
        assertTrue(value instanceof Number, "Value must be a number!");
        assertEquals(125.0D, ((Number) value).doubleValue());
    }

}
