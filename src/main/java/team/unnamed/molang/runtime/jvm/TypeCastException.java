package team.unnamed.molang.runtime.jvm;

/**
 * Thrown to indicate that the code has attempted to cast a type
 * to another type, and they were not compatible.
 */
public final class TypeCastException extends RuntimeException {
    private static final long serialVersionUID = -1289858918925812801L;

    public TypeCastException() {
    }

    public TypeCastException(final String message) {
        super(message);
    }
}
