package team.unnamed.molang.runtime.jvm;

import javassist.CtClass;
import org.jetbrains.annotations.Nullable;

final class CompileVisitResult {
    private final CtClass lastPushedType;

    public CompileVisitResult(final @Nullable CtClass lastPushedType) {
        this.lastPushedType = lastPushedType;
    }

    public @Nullable CtClass lastPushedType() {
        return lastPushedType;
    }

    public boolean isString() {
        return lastPushedType != null && lastPushedType.getName().equals("java.lang.String");
    }

    public boolean is(final @Nullable CtClass type) {
        return lastPushedType != null && lastPushedType.equals(type);
    }
}
