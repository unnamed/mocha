package team.unnamed.molang;

import org.junit.jupiter.api.Test;
import team.unnamed.molang.runtime.Function;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ForEachTest {
    @Test
    void test() throws Exception {
        final MolangEngine engine = MolangEngine.builder()
                .withDefaultBindings()
                .bindVariable("list_people", (Function) (ctx, args) -> Arrays.asList("Andre", "John", "Ian", "Salva"))
                .build();

        Object result;
        try (Reader reader = new InputStreamReader(ForEachTest.class.getClassLoader().getResourceAsStream("for_each.molang"))) {
            result = engine.eval(reader);
        }
        assertEquals("Andre, John, Ian, Salva", result);
    }
}
