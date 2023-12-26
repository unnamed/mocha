package team.unnamed.mocha.runtime.value;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

public interface ObjectValue extends Value {
    @NotNull Value get(final @NotNull String name);

    default boolean set(final @NotNull String name, final @Nullable Value value) {
        return false;
    }

    default @NotNull Map<String, Value> entries() {
        return Collections.emptyMap();
    }
}
