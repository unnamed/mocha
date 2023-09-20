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

        MolangEngine.Builder builder = MolangEngine.builder()
                .withDefaultBindings();
        builder.bindings.setProperty("query", StandardBindings.createQueryBinding(() -> stdout));
        MolangEngine engine = builder.build();
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
