package team.unnamed.molang;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PrioritiesTest {

    private static ScriptEngine engine;

    @BeforeAll
    public static void setEngine() {
        engine = new ScriptEngineManager().getEngineByName("molang");;
    }

    @Test
    public void test() {
        try {
            // sin(90) * 5 + 10 + cos(90)
            //    1    * 5 + 10 +   0
            //         5   +    10
            //             15
            assertEquals(15F, engine.eval("maTh.SiN(90) * 5 + 10 + MATH.coS(90.000)"));

            // abs(-10) + sqrt(25)
            //    10    +    5
            //         15
            assertEquals(15F, engine.eval("maTH.aBS(-10) + MATh.SQRT(25)"));

            // abs(-10) * 72 + sqrt(49)
            //    10    * 72 +    7
            //         720   +    7
            //              727
            assertEquals(727F, engine.eval("mATH.abs(-10) * 72 + MATH.sqRT(49)"));
        } catch (ScriptException e) {
            Assertions.fail(e);
        }
    }

}
