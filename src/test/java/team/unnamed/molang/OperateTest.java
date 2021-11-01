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
        assertEquals(1, "5 <= 10");
    }

    @Test
    public void test_logical() {
        assertEquals(1, "true && true");
        assertEquals(1, "true || false");
        assertEquals(0, "false && true");
        assertEquals(0, "false || false");
    }

}
