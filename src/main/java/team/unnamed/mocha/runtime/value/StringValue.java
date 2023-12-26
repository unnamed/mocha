package team.unnamed.mocha.runtime.value;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.util.Objects.requireNonNull;

public final class StringValue implements Value {
    private final String value;

    private StringValue(final @NotNull String value) {
        this.value = requireNonNull(value, "value");
    }

    public static @NotNull StringValue of(final @NotNull String value) {
        return new StringValue(value);
    }

    public @NotNull String value() {
        return value;
    }

    @Override
    public @NotNull String toString() {
        return "StringValue(\"" + value + "\")";
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final StringValue that = (StringValue) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
