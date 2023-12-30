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

/**
 * Represents a {@link ObjectValue} property.
 *
 * @since 3.0.0
 */
public interface ObjectProperty {
    /**
     * Creates a new {@link ObjectProperty} with the given
     * {@link Value} and constant flag.
     *
     * @param value    The value of the property
     * @param constant Whether the property is constant or not
     * @return The created property
     * @since 3.0.0
     */
    static @NotNull ObjectProperty property(final @NotNull Value value, final boolean constant) {
        return new ObjectPropertyImpl(value, constant);
    }
    
    /**
     * Returns the value of this property.
     *
     * @return The value of this property
     * @since 3.0.0
     */
    @NotNull Value value();

    /**
     * Determines whether this property is constant and cannot
     * be changed in runtime.
     *
     * @return Whether this property is constant or not
     * @since 3.0.0
     */
    boolean constant();
}
