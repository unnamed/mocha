package team.unnamed.molang;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import team.unnamed.molang.parser.MoLangParser;
import team.unnamed.molang.parser.ParseException;
import team.unnamed.molang.parser.StandardMoLangParser;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class NoOutputTests {

    private static ScriptEngine engine;

    @BeforeAll
    public static void initParser() {
        engine = new MoLangScriptEngine(new MoLangScriptEngineFactory(), new StandardMoLangParser());
    }

    private void parseResource(String resource) throws IOException {
        InputStream input = NoOutputTests.class.getClassLoader().getResourceAsStream(resource);
        if (input == null) {
            Assertions.fail("Invalid resource: " + resource);
        }
        try (Reader reader = new InputStreamReader(input)) {
            try {
                engine.eval(reader);
            } catch (ScriptException e) {
                Assertions.fail("Failed to parse " + resource, e);
            }
        }
    }

    @Test
    public void test() {
        try {
            parseResource("test.molang");
        } catch (IOException e) {
            Assertions.fail("Failed to open resources", e);
        }
    }

}
