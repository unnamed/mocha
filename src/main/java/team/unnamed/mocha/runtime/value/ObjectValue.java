package team.unnamed.mocha.runtime.value;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

public interface ObjectValue extends Value {
    @NotNull Value get(final @NotNull String name);

    default boolean set(final @NotNull String name, final @Nullable Value value) {
        return false;
    }

    default @NotNull Map<String, Value> entries() {
        return Collections.emptyMap();
    }

    // :) overloads
    default void setFunction(final @NotNull String name, final @NotNull DoubleFunction1 function) {
        set(name, (Function<?>) (ctx, args) -> NumberValue.of(function.apply(args.next().eval().getAsNumber())));
    }

    default void setFunction(final @NotNull String name, final @NotNull DoubleFunction2 function) {
        set(name, (Function<?>) (ctx, args) -> NumberValue.of(function.apply(args.next().eval().getAsNumber(), args.next().eval().getAsNumber())));
    }

    default void setFunction(final @NotNull String name, final @NotNull DoubleFunction3 function) {
        set(name, (Function<?>) (ctx, args) -> NumberValue.of(function.apply(args.next().eval().getAsNumber(), args.next().eval().getAsNumber(), args.next().eval().getAsNumber())));
    }

    interface DoubleFunction1 {
        double apply(double n);
    }

    interface DoubleFunction2 {
        double apply(double n1, double n2);
    }

    interface DoubleFunction3 {
        double apply(double n1, double n2, double n3);
    }
}
