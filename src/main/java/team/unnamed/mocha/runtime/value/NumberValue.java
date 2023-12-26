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
