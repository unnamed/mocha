package team.unnamed.mocha.runtime.value;

import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

public final class ArrayValue implements Value {
    private final Value[] values;

    private ArrayValue(final @NotNull Value @NotNull ... values) {
        this.values = requireNonNull(values, "values");
    }

    public static @NotNull ArrayValue of(final @NotNull Value @NotNull ... values) {
        return new ArrayValue(values);
    }

    public @NotNull Value @NotNull [] values() {
        return values;
    }

    public @NotNull Value get(final Value index) {
        return values[(int) index.getAsNumber()];
    }
}
