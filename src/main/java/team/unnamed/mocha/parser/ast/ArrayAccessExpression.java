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
package team.unnamed.mocha.parser.ast;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * Array accessing expression implementation, access to a value in
 * an array, by its index.
 *
 * <p>Example array accessing expressions: {@code my_geometries[0]},
 * {@code array.my_geometries[query.anim_time]}, {@code array.my_geos[math.cos(90)]}</p>
 *
 * @since 3.0.0
 */
public final class ArrayAccessExpression implements Expression {
    private Expression array;
    private Expression index;

    public ArrayAccessExpression(final @NotNull Expression array, final @NotNull Expression index) {
        this.array = requireNonNull(array, "array");
        this.index = requireNonNull(index, "index");
    }

    /**
     * Gets the 'array' expression, the index is evaluated on this
     * expression's result.
     *
     * @return The array expression.
     * @since 3.0.0
     */
    public @NotNull Expression array() {
        return array;
    }

    /**
     * Sets the 'array' expression, the index is evaluated on this
     * expression's result.
     *
     * @param array The new array expression.
     * @since 3.0.0
     */
    public void array(final @NotNull Expression array) {
        this.array = requireNonNull(array, "array");
    }

    /**
     * Gets the 'index' expression, the index is evaluated on this
     * expression's result.
     *
     * @return The index expression.
     * @since 3.0.0
     */
    public @NotNull Expression index() {
        return index;
    }

    /**
     * Sets the 'index' expression, the index is evaluated on this
     * expression's result.
     *
     * @param index The new index expression.
     * @since 3.0.0
     */
    public void index(final @NotNull Expression index) {
        this.index = requireNonNull(index, "index");
    }

    @Override
    public <R> R visit(final @NotNull ExpressionVisitor<R> visitor) {
        return visitor.visitArrayAccess(this);
    }

    @Override
    public @NotNull String toString() {
        return "ArrayAccess(" + array + ", " + index + ")";
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ArrayAccessExpression that = (ArrayAccessExpression) o;
        if (!array.equals(that.array)) return false;
        return index.equals(that.index);
    }

    @Override
    public int hashCode() {
        int result = array.hashCode();
        result = 31 * result + index.hashCode();
        return result;
    }
}
