package team.unnamed.mocha.runtime;

import org.jetbrains.annotations.NotNull;
import team.unnamed.mocha.runtime.value.MutableObjectBinding;
import team.unnamed.mocha.runtime.value.ObjectValue;
import team.unnamed.mocha.runtime.value.Value;

public interface GlobalScope extends ObjectValue {
    static @NotNull GlobalScope create() {
        return new GlobalScopeImpl();
    }

    @NotNull GlobalScope copy();

    void forceSet(final @NotNull String name, final @NotNull Value value);

    default @NotNull MutableObjectBinding query() {
        if (entries().containsKey("query")) {
            return (MutableObjectBinding) get("query");
        } else {
            final MutableObjectBinding query = new MutableObjectBinding();
            forceSet("query", query);
            return query;
        }
    }
}
