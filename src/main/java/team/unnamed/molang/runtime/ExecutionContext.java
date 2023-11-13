package team.unnamed.molang.runtime;

public final class ExecutionContext<T> {
    private final T entity;

    public ExecutionContext(T entity) {
        this.entity = entity;
    }

    public T entity() {
        return entity;
    }
}
