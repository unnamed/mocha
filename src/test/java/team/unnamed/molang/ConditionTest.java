package team.unnamed.molang;

import org.junit.jupiter.api.Test;

import static team.unnamed.molang.MoLangAssertions.assertEquals;

public class ConditionTest {

    @Test
    public void test_basic() {
        assertEquals(2, "true ? 1 + 1");
        assertEquals(0, "false ? 200 * 500");
        assertEquals(10, "(5 * 10 < 100) ? 10 : 20");
        assertEquals(5, "10 * 10 ? 5 : 10");
        assertEquals(7, "((10 * 10) > 20) ? 7 : 27");
    }

}
