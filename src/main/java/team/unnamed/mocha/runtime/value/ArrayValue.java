package team.unnamed.mocha.runtime.value;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

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

    @Override
    public @NotNull String toString() {
        return "ArrayValue[" + Arrays.toString(values) + "]";
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ArrayValue that = (ArrayValue) o;
        return Arrays.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }
}
