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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mocha.runtime.value.ObjectProperty;
import team.unnamed.mocha.runtime.value.Value;
import team.unnamed.mocha.util.CaseInsensitiveStringHashMap;

import java.util.Map;

final class GlobalScopeImpl implements GlobalScope {
    private final Map<String, ObjectProperty> bindings = new CaseInsensitiveStringHashMap<>();

    @Override
    public @Nullable ObjectProperty getProperty(final @NotNull String name) {
        return bindings.get(name);
    }

    @Override
    public @NotNull GlobalScope copy() {
        final GlobalScopeImpl copy = new GlobalScopeImpl();
        copy.bindings.putAll(this.bindings);
        return copy;
    }

    @Override
    public void forceSet(final @NotNull String name, final @NotNull Value value) {
        bindings.put(name, ObjectProperty.property(value, false));
    }

    @Override
    public @NotNull Map<String, ObjectProperty> entries() {
        return bindings;
    }

    static final class BuilderImpl implements Builder {
        private final Map<String, ObjectProperty> properties = new CaseInsensitiveStringHashMap<>();

        @Override
        public Builder set(@NotNull String name, @NotNull Value value) {
            properties.put(name, ObjectProperty.property(value, true));
            return this;
        }

        @Override
        public GlobalScope build() {
            GlobalScopeImpl impl = new GlobalScopeImpl();
            impl.bindings.putAll(properties);
            return impl;
        }
    }
}
