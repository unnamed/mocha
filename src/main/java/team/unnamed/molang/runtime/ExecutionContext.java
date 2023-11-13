package team.unnamed.molang.runtime;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.molang.parser.ast.Expression;

public interface ExecutionContext<T> {
    T entity();

    @Nullable Object eval(final @NotNull Expression expression);
}
