package team.unnamed.molang.runtime.jvm;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.bytecode.Bytecode;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

final class FunctionCompileState {
    private final MolangCompilerImpl compiler;

    private final ClassPool classPool;
    private final CtClass ctClass;
    private final Bytecode bytecode;
    private final Method method;

    private final Map<String, Object> requirements = new HashMap<>();
    private final Map<String, RegisteredMolangNative> natives;
    private final Map<String, Integer> argumentParameterIndexes;

    FunctionCompileState(
            MolangCompilerImpl compiler,
            ClassPool classPool,
            CtClass ctClass, Bytecode bytecode,
            Method method,
            Map<String, RegisteredMolangNative> natives,
            Map<String, Integer> argumentParameterIndexes
    ) {
        this.compiler = requireNonNull(compiler, "compiler");
        this.classPool = requireNonNull(classPool, "classPool");
        this.ctClass = requireNonNull(ctClass, "ctClass");
        this.bytecode = requireNonNull(bytecode, "bytecode");
        this.method = requireNonNull(method, "method");
        this.natives = requireNonNull(natives, "natives");
        this.argumentParameterIndexes = requireNonNull(argumentParameterIndexes, "argumentParameterIndexes");
    }

    public @NotNull ClassPool classPool() {
        return classPool;
    }

    public @NotNull CtClass type() {
        return ctClass;
    }

    public @NotNull Bytecode bytecode() {
        return bytecode;
    }

    public @NotNull Method method() {
        return method;
    }

    public @NotNull Map<String, Object> requirements() {
        return requirements;
    }

    public @NotNull Map<String, RegisteredMolangNative> natives() {
        return natives;
    }

    public @NotNull Map<String, Integer> argumentParameterIndexes() {
        return argumentParameterIndexes;
    }
}
