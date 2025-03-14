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
package team.unnamed.mocha.parser.ast;

import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

/**
 * Property accessing expression implementation,
 * access to a property on another expression result.
 *
 * <p>Example property accessing expressions: {@code v.x},
 * {@code v.location.x}, {@code 'str'.length}, {@code query.print}</p>
 *
 * @since 3.0.0
 */
public final class AccessExpression implements Expression {

    private final String property;
    private Expression object;

    public AccessExpression(final @NotNull Expression object, final @NotNull String property) {
        this.object = requireNonNull(object, "object");
        this.property = requireNonNull(property, "property");
    }

    /**
     * Gets the "object" expression, the property is
     * evaluated on this expression's result.
     *
     * @return The object expression.
     * @since 3.0.0
     */
    public @NotNull Expression object() {
        return object;
    }

    /**
     * Sets the "object" expression, the property is
     * evaluated on this expression's result.
     *
     * @param object The object expression.
     * @since 3.0.0
     */
    public void object(final @NotNull Expression object) {
        this.object = requireNonNull(object, "object");
    }

    /**
     * Gets the accessed property name.
     *
     * @return The property name.
     * @since 3.0.0
     */
    public @NotNull String property() {
        return property;
    }

    @Override
    public <R> R visit(final @NotNull ExpressionVisitor<R> visitor) {
        return visitor.visitAccess(this);
    }

    @Override
    public String toString() {
        return "Access(" + object + ", " + property + ")";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessExpression that = (AccessExpression) o;
        if (!object.equals(that.object)) return false;
        return property.equals(that.property);
    }

    @Override
    public int hashCode() {
        int result = object.hashCode();
        result = 31 * result + property.hashCode();
        return result;
    }

}