package team.unnamed.mocha.runtime.binding;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mocha.runtime.ExecutionContext;
import team.unnamed.mocha.runtime.value.Function;
import team.unnamed.mocha.runtime.value.Value;

import java.lang.reflect.Method;

@ApiStatus.Internal
public final class JavaFunction<T> implements Function<T> {
    private final Object object;
    private final Method method;
    private Function<T> function;

    JavaFunction(final @Nullable Object object, final @Nullable Method method, final @Nullable Function<T> function) {
        this.object = object;
        this.method = method;
        this.function = function;
        evaluate();
    }

    private void evaluate() {
        if (function == null) {
            if (method == null) {
                throw new IllegalArgumentException("Either the method or a generic function must be given.");
            }

            // create the generic function from the method
            this.function = new ReflectiveFunction<>(object, method);
        }
    }

    public @Nullable Object object() {
        return object;
    }

    public @Nullable Method method() {
        return method;
    }

    @Override
    public @Nullable Value evaluate(final @NotNull ExecutionContext<T> context, final @NotNull Arguments arguments) {
        return function.evaluate(context, arguments);
    }
}
