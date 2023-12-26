package team.unnamed.mocha.runtime;

import org.jetbrains.annotations.NotNull;
import team.unnamed.mocha.runtime.value.NumberValue;
import team.unnamed.mocha.runtime.value.Value;

import java.util.HashMap;
import java.util.Map;

final class GlobalScopeImpl implements GlobalScope {
    private final Map<String, Value> bindings = new HashMap<>();

    @Override
    public @NotNull Value get(final @NotNull String name) {
        return bindings.getOrDefault(name, NumberValue.zero());
    }

    @Override
    public @NotNull GlobalScope copy() {
        final GlobalScopeImpl copy = new GlobalScopeImpl();
        copy.bindings.putAll(this.bindings);
        return copy;
    }

    @Override
    public void forceSet(final @NotNull String name, final @NotNull Value value) {
        bindings.put(name, value);
    }
}
