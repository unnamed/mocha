package team.unnamed.molang;

import org.junit.jupiter.api.Test;

import static team.unnamed.molang.MoLangAssertions.assertEquals;

public class OperateTest {

    @Test
    public void test_basic() {
        assertEquals(2, "1 + 1");
        assertEquals(3, "1 + 1 * 2");
        assertEquals(10, "true ? 10");
        assertEquals(10, "false ? 5 : 10");
        assertEquals(0, "3 >= 4");
    }

}
