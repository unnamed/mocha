package team.unnamed.mocha.runtime.value;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NumberValue implements Value {
    private static final NumberValue ZERO = new NumberValue(0D);

    private final double value;

    private NumberValue(final double value) {
        this.value = normalize(value);
    }

    public static @NotNull NumberValue of(final double value) {
        return new NumberValue(value);
    }

    public static @NotNull NumberValue zero() {
        return ZERO;
    }

    public static double normalize(final double value) {
        return Double.isNaN(value) || Double.isInfinite(value) ? 0D : value;
    }

    public double value() {
        return value;
    }

    @Override
    public @NotNull String toString() {
        return "NumberValue(" + value + ')';
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final NumberValue that = (NumberValue) o;
        return Double.compare(that.value, value) == 0;
    }

    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(value);
        return (int) (temp ^ (temp >>> 32));
    }
}
