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
package team.unnamed.mocha.runtime;

import org.jetbrains.annotations.NotNull;
import team.unnamed.mocha.runtime.value.ObjectValue;
import team.unnamed.mocha.runtime.value.Value;

/**
 * Represents a scope, which is just a virtual object that
 * contains bindings (properties) to values.
 *
 * @since 3.0.0
 */
public interface Scope extends ObjectValue {
    static @NotNull Scope create() {
        return new ScopeImpl();
    }

    static @NotNull Builder builder() {
        return new ScopeImpl.BuilderImpl();
    }

    /**
     * Creates a shallow copy of this scope. The copy will
     * contain the same bindings as this scope, but it will
     * be a different object, and changes (only) to the copy
     * properties will not affect this scope.
     *
     * @return The shallow copy of this scope.
     * @since 3.0.0
     */
    @NotNull Scope copy();

    void readOnly(final boolean readOnly);

    boolean readOnly();

    interface Builder {
        Builder set(final @NotNull String name, final @NotNull Value value);

        Scope build();
    }
}
