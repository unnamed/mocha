package team.unnamed.molang.binding;

/**
 * Class holding some default bindings and
 * static utility methods for ease working
 * with bindings
 */
public final class Bind {

    /**
     * Default bindings for math
     * @see MathBinding
     */
    public static final ObjectBinding MATH_BINDING = new MathBinding();

    /**
     * Default bindings for queries
     * @see QueryBinding
     */
    public static final ObjectBinding QUERY_BINDING = new QueryBinding();

    private Bind() {
    }

}
