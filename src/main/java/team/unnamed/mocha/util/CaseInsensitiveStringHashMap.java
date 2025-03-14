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
package team.unnamed.mocha.util;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A {@link HashMap} implementation that uses case-insensitive
 * {@link String}s as keys. In this kind of map, all keys are
 * lower-cased before they are added.
 *
 * <p>Listing keys will return all the keys in lowercase.</p>
 *
 * @param <V> The value type
 * @see HashMap
 * @since 3.0.0
 */
public class CaseInsensitiveStringHashMap<V> extends HashMap<String, V> {
    private Set<String> keySet;
    private Set<Map.Entry<String, V>> entrySet;

    public CaseInsensitiveStringHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public CaseInsensitiveStringHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    public CaseInsensitiveStringHashMap() {
    }

    public CaseInsensitiveStringHashMap(Map<String, ? extends V> m) {
        super(lowercaseMap(m));
    }

    private static <T> T lowercase(T key) {
        //noinspection unchecked
        return key instanceof String ? ((T) ((String) key).toLowerCase()) : key;
    }

    private static <V> Map<? extends String, ? extends V> lowercaseMap(Map<? extends String, ? extends V> m) {
        final Map<String, V> lowercased = new HashMap<>();
        for (Entry<? extends String, ? extends V> entry : m.entrySet()) {
            lowercased.put(lowercase(entry.getKey()), entry.getValue());
        }
        return lowercased;
    }

    @Override
    public V get(Object key) {
        return super.get(lowercase(key));
    }

    @Override
    public boolean containsKey(Object key) {
        return super.containsKey(lowercase(key));
    }

    @Override
    public V put(String key, V value) {
        return super.put(lowercase(key), value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> m) {
        super.putAll(lowercaseMap(m));
    }

    @Override
    public V remove(Object key) {
        return super.remove(lowercase(key));
    }

    @Override
    public Set<String> keySet() {
        Set<String> ks = keySet;
        if (ks == null) {
            ks = new CaseInsensitiveKeySet(super.keySet());
            keySet = ks;
        }
        return ks;
    }

    @Override
    public Set<Entry<String, V>> entrySet() {
        Set<Map.Entry<String, V>> es;
        return (es = entrySet) == null ? (entrySet = new CaseInsensitiveEntrySet(super.entrySet())) : es;
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return super.getOrDefault(lowercase(key), defaultValue);
    }

    @Override
    public V putIfAbsent(String key, V value) {
        return super.putIfAbsent(lowercase(key), value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return super.remove(lowercase(key), value);
    }

    @Override
    public boolean replace(String key, V oldValue, V newValue) {
        return super.replace(lowercase(key), oldValue, newValue);
    }

    @Override
    public V replace(String key, V value) {
        return super.replace(lowercase(key), value);
    }

    @Override
    public V computeIfAbsent(String key, @NotNull Function<? super String, ? extends V> mappingFunction) {
        return super.computeIfAbsent(lowercase(key), mappingFunction);
    }

    @Override
    public V computeIfPresent(String key, @NotNull BiFunction<? super String, ? super V, ? extends V> remappingFunction) {
        return super.computeIfPresent(lowercase(key), remappingFunction);
    }

    @Override
    public V compute(String key, @NotNull BiFunction<? super String, ? super V, ? extends V> remappingFunction) {
        return super.compute(lowercase(key), remappingFunction);
    }

    @Override
    public V merge(String key, @NotNull V value, @NotNull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return super.merge(lowercase(key), value, remappingFunction);
    }

    private final class CaseInsensitiveKeySet extends AbstractSet<String> {
        private final Set<String> internalkeySet;

        CaseInsensitiveKeySet(Set<String> internalkeySet) {
            this.internalkeySet = internalkeySet;
        }

        @Override
        public int size() {
            return CaseInsensitiveStringHashMap.this.size();
        }

        @Override
        public void clear() {
            CaseInsensitiveStringHashMap.this.clear();
        }

        @Override
        public @NotNull Iterator<String> iterator() {
            return internalkeySet.iterator();
        }

        @Override
        public boolean contains(Object o) {
            return CaseInsensitiveStringHashMap.this.containsKey(o);
        }

        @Override
        public boolean remove(Object o) {
            return internalkeySet.remove(lowercase(o));
        }

        @Override
        public Spliterator<String> spliterator() {
            return internalkeySet.spliterator();
        }

        @Override
        public Object @NotNull [] toArray() {
            return internalkeySet.toArray();
        }

        @Override
        public void forEach(Consumer<? super String> action) {
            internalkeySet.forEach(action);
        }
    }

    private final class CaseInsensitiveEntrySet extends AbstractSet<Map.Entry<String, V>> {
        private final Set<Map.Entry<String, V>> internalEntrySet;

        CaseInsensitiveEntrySet(Set<Map.Entry<String, V>> internalEntrySet) {
            this.internalEntrySet = internalEntrySet;
        }

        @Override
        public int size() {
            return CaseInsensitiveStringHashMap.this.size();
        }

        @Override
        public void clear() {
            CaseInsensitiveStringHashMap.this.clear();
        }

        @Override
        public @NotNull Iterator<Map.Entry<String, V>> iterator() {
            return internalEntrySet.iterator();
        }

        private Object convertEntry(Object o) {
            if (!(o instanceof Map.Entry<?, ?>))
                return o;
            final Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            final Object key = lowercase(e.getKey());
            final Object value = e.getValue();
            return new AbstractMap.SimpleEntry<>(key, value);
        }

        @Override
        public boolean contains(Object o) {
            return internalEntrySet.contains(convertEntry(o));
        }

        @Override
        public boolean remove(Object o) {
            return internalEntrySet.remove(convertEntry(o));
        }

        @Override
        public Spliterator<Map.Entry<String, V>> spliterator() {
            return internalEntrySet.spliterator();
        }

        @Override
        public void forEach(Consumer<? super Map.Entry<String, V>> action) {
            internalEntrySet.forEach(action);
        }
    }
}
