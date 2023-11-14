package team.unnamed.molang.runtime.jvm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

import static java.util.Objects.requireNonNull;

final class RegisteredMolangNative {
    private final String name;
    private final Class<?> clazz;
    private final Object object;
    private final Method method;

    RegisteredMolangNative(String name, Class<?> clazz, Object object, Method method) {
        this.name = requireNonNull(name, "name");
        this.clazz = clazz;
        this.object = object;
        this.method = requireNonNull(method, "method");
    }

    public @NotNull String name() {
        return name;
    }

    public @Nullable Object object() {
        return object;
    }

    public @NotNull Method method() {
        return method;
    }
}
