package team.unnamed.mocha.runtime.value;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ValueOfTest {
    @Test
    void test() {
        assertEquals(NumberValue.of(5), Value.of(5));
        assertEquals(StringValue.of("hello"), Value.of("hello"));
        assertEquals(NumberValue.of(1), Value.of(true));
        assertEquals(NumberValue.zero(), Value.of(false));
        assertEquals(NumberValue.zero(), Value.of(null));
        assertEquals(NumberValue.zero(), Value.of(new Object()));
        assertEquals(ArrayValue.of(), Value.of(new Object[0]));
        assertEquals(ArrayValue.of(), Value.of(new int[0]));
        assertEquals(ArrayValue.of(), Value.of(new double[0]));
        assertEquals(ArrayValue.of(NumberValue.of(1)), Value.of(new int[]{1}));
        assertEquals(ArrayValue.of(NumberValue.of(1)), Value.of(new double[]{1}));
        assertEquals(ArrayValue.of(NumberValue.of(1)), Value.of(new Object[]{1}));
        assertEquals(ArrayValue.of(NumberValue.of(1), NumberValue.of(2)), Value.of(new int[]{1, 2}));
        assertEquals(ArrayValue.of(NumberValue.of(1), NumberValue.of(2)), Value.of(new double[]{1, 2}));
        assertEquals(ArrayValue.of(NumberValue.of(1), StringValue.of("hello"), NumberValue.zero()), Value.of(new Object[]{1, "hello", false}));
    }
}
