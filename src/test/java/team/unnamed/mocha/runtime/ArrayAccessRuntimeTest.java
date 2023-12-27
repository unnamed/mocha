package team.unnamed.mocha.runtime;

import org.junit.jupiter.api.Test;
import team.unnamed.mocha.MochaEngine;
import team.unnamed.mocha.runtime.value.ArrayValue;
import team.unnamed.mocha.runtime.value.NumberValue;

import java.util.function.UnaryOperator;

import static team.unnamed.mocha.MochaAssertions.assertEvaluates;

class ArrayAccessRuntimeTest {
    @Test
    void test() {
        final UnaryOperator<MochaEngine<?>> configurer = engine -> {
            engine.scope().query().set("values", ArrayValue.of(
                    NumberValue.of(5D),
                    NumberValue.of(10D),
                    NumberValue.of(100D)
            ));
            return engine;
        };

        assertEvaluates(5D, "query.values[0]", configurer);
        assertEvaluates(10D, "query.values[1]", configurer);
        assertEvaluates(100D, "query.values[2]", configurer);
        assertEvaluates(100D, "q.values[20 + 3]", configurer);
        assertEvaluates(5D, "query.values[-1]", configurer);
        assertEvaluates(5D, "q.values[-1000]", configurer);
        assertEvaluates(5D, "q.values[0.5]", configurer);
        assertEvaluates(10D, "q.values[0.5 + 0.5]", configurer);
        assertEvaluates(100D, "q.values[math.pi - 1]", configurer);
    }
}
