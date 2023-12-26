package team.unnamed.mocha.runtime.value;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.StringJoiner;

@ApiStatus.NonExtendable
public /* sealed */ interface Value /* permits Function, ObjectValue, ArrayValue, NumberValue, StringValue */ {
    static @NotNull Value of(final @Nullable Object any) {
        if (any instanceof Value) {
            return (Value) any;
        } else if (any instanceof Number) {
            return NumberValue.of(((Number) any).doubleValue());
        } else if (any instanceof String) {
            return StringValue.of((String) any);
        } else if (any instanceof Boolean) {
            return (Boolean) any ? NumberValue.of(1D) : NumberValue.zero();
        } else {
            if (any != null && any.getClass().isArray()) {
                // array types
                final int length = Array.getLength(any);
                final Value[] values = new Value[length];
                for (int i = 0; i < length; i++) {
                    values[i] = of(Array.get(any, i));
                }
                return ArrayValue.of(values);
            } else {
                return NumberValue.zero();
            }
        }
    }

    static @NotNull Value of(final boolean bool) {
        return bool ? NumberValue.of(1D) : NumberValue.zero();
    }

    default double getAsNumber() {
        if (this instanceof NumberValue) {
            return ((NumberValue) this).value();
        } else {
            return 0;
        }
    }

    default boolean getAsBoolean() {
        if (this instanceof NumberValue) {
            return ((NumberValue) this).value() != 0D;
        } else if (this instanceof StringValue) {
            return !((StringValue) this).value().isEmpty();
        } else if (this instanceof ArrayValue) {
            return ((ArrayValue) this).values().length != 0;
        } else if (this instanceof ObjectValue) {
            return !((ObjectValue) this).entries().isEmpty();
        } else {
            return true;
        }
    }

    default boolean isString() {
        return this instanceof StringValue;
    }

    default @NotNull String getAsString() {
        if (this instanceof StringValue) {
            return ((StringValue) this).value();
        } else if (this instanceof NumberValue) {
            return Double.toString(((NumberValue) this).value());
        } else if (this instanceof ArrayValue) {
            final Value[] values = ((ArrayValue) this).values();
            final StringJoiner joiner = new StringJoiner(", ", "[", "]");
            for (final Value value : values) {
                joiner.add(value.getAsString());
            }
            return joiner.toString();
        } else if (this instanceof ObjectValue) {
            final Map<String, Value> values = ((ObjectValue) this).entries();
            final StringJoiner joiner = new StringJoiner(", ", "{", "}");
            for (final Map.Entry<String, Value> entry : values.entrySet()) {
                joiner.add(entry.getKey() + ": " + entry.getValue().getAsString());
            }
            return joiner.toString();
        } else if (this instanceof Function<?>) {
            return "Function(" + this + ")";
        } else {
            throw new IllegalArgumentException("Unknown value type: " + this.getClass().getName());
        }
    }
}
