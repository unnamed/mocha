package team.unnamed.molang.runtime.jvm;

import org.junit.jupiter.api.Test;

class MolangCompilerTest {
    @Test
    void test() {
        MolangCompiler compiler = MolangCompiler.compiler();
        ScriptType script = compiler.compile("false ? a : b", ScriptType.class);
        System.out.println(script.eval(1, 2));
    }

    interface ScriptType extends MolangFunction {
        int eval(@Named("a") double a, @Named("b") double b);
    }
}
