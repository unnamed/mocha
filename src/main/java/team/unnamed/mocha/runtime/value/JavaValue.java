package team.unnamed.mocha.runtime.value;

public final class JavaValue implements Value {
    private final Object value;

    public JavaValue(Object value) {
        this.value = value;
    }

    public Object value() {
        return value;
    }
}
