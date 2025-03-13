/*
 * This file is part of mocha, licensed under the MIT license
 *
 * Copyright (c) 2021-2023 Unnamed Team
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
package team.unnamed.mocha.runtime;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mocha.runtime.value.Value;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@ApiStatus.Internal
public final class JavaTypes {
    private static final Map<Class<?>, Object> NULL_VALUES = new HashMap<>();

    static {
        NULL_VALUES.put(int.class, 0);
        NULL_VALUES.put(long.class, 0L);
        NULL_VALUES.put(float.class, 0F);
        NULL_VALUES.put(double.class, 0D);
        NULL_VALUES.put(short.class, (short) 0);
        NULL_VALUES.put(byte.class, (byte) 0);
        NULL_VALUES.put(char.class, '\0');
        NULL_VALUES.put(boolean.class, false);
        NULL_VALUES.put(String.class, "");
    }

    private JavaTypes() {
    }

    public static @Nullable Object getNullValueForType(final @NotNull Class<?> type) {
        requireNonNull(type, "type");
        return NULL_VALUES.get(type);
    }

    public static @Nullable Object convert(final @NotNull Value value, final @NotNull Class<?> type) {
        requireNonNull(value, "value");
        requireNonNull(type, "type");
        if (type == String.class) {
            return value.getAsString();
        } else if (type == Double.class || type == double.class) {
            return value.getAsNumber();
        } else if (type == Integer.class || type == int.class) {
            return (int) value.getAsNumber();
        } else if (type == Float.class || type == float.class) {
            return (float) value.getAsNumber();
        } else if (type == Boolean.class || type == boolean.class) {
            return value.getAsBoolean();
        } else if (type == Long.class || type == long.class) {
            return (long) value.getAsNumber();
        } else if (type == Short.class || type == short.class) {
            return (short) value.getAsNumber();
        } else {
            return null;
        }
    }
}