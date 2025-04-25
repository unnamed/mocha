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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NumberValue implements Value {
    private static final NumberValue ONE = new NumberValue(1F);
    private static final NumberValue ZERO = new NumberValue(0F);

    private final float value;

    private NumberValue(final float value) {
        this.value = normalize(value);
    }

    public static @NotNull NumberValue of(final double value) {
        return NumberValue.of((float) value);
    }

    public static @NotNull NumberValue of(final float value) {
        if (value == ONE.value()) return ONE;
        if (value == ZERO.value()) return ZERO;
        return new NumberValue(value);
    }

    public static @NotNull NumberValue one() {
        return ONE;
    }

    public static @NotNull NumberValue zero() {
        return ZERO;
    }

    public static double normalize(final double value) {
        return Double.isNaN(value) || Double.isInfinite(value) ? 0D : value;
    }

    public static float normalize(final float value) {
        return Float.isNaN(value) || Float.isInfinite(value) ? 0F : value;
    }

    public float value() {
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
        return Float.compare(that.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Float.hashCode(value);
    }
}
