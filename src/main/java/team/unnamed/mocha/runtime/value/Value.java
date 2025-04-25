/*
 * This file is part of mocha, licensed under the MIT license
 *
 * Copyright (c) 2021-2025 Unnamed Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
            return NumberValue.of(((Number) any).floatValue());
        } else if (any instanceof String) {
            return StringValue.of((String) any);
        } else if (any instanceof Boolean) {
            return (Boolean) any ? NumberValue.one() : NumberValue.zero();
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
        return bool ? NumberValue.one() : NumberValue.zero();
    }

    static @NotNull Value of(final double _double) {
        return NumberValue.of(_double);
    }

    static @NotNull Value of(final float _float) {
        return NumberValue.of(_float);
    }

    static @NotNull Value of(final @Nullable String string) {
        return string == null ? nil() : StringValue.of(string);
    }

    static @NotNull Value nil() {
        return NumberValue.zero();
    }

    default float getAsNumber() {
        if (this instanceof NumberValue) {
            return ((NumberValue) this).value();
        } else {
            return 0;
        }
    }

    default boolean getAsBoolean() {
        if (this instanceof NumberValue) {
            return ((NumberValue) this).value() != 0F;
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
            return Float.toString(((NumberValue) this).value());
        } else if (this instanceof ArrayValue) {
            final Value[] values = ((ArrayValue) this).values();
            final StringJoiner joiner = new StringJoiner(", ", "[", "]");
            for (final Value value : values) {
                joiner.add(value.getAsString());
            }
            return joiner.toString();
        } else if (this instanceof ObjectValue) {
            final Map<String, ObjectProperty> values = ((ObjectValue) this).entries();
            final StringJoiner joiner = new StringJoiner(", ", "{", "}");
            for (final Map.Entry<String, ObjectProperty> entry : values.entrySet()) {
                joiner.add(entry.getKey() + ": " + entry.getValue().value().getAsString());
            }
            return joiner.toString();
        } else if (this instanceof Function<?>) {
            return "Function(" + this + ")";
        } else {
            throw new IllegalArgumentException("Unknown value type: " + this.getClass().getName());
        }
    }
}
