package team.unnamed.molang.runtime.binding;

import java.io.PrintStream;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Class holding some default bindings and
 * static utility methods for ease working
 * with bindings
 */
public final class StandardBindings {

    public static final ObjectBinding BUILT_IN = createBuiltIn();
    public static final ObjectBinding MATH_BINDING = new MathBinding();
    public static final ObjectBinding QUERY_BINDING = createQueryBinding(() -> System.out);

    private StandardBindings() {
    }

    private static ObjectBinding createBuiltIn() {
        ObjectBinding o = new ObjectBinding();
        o.setProperty("loop", (CallableBinding) (args) -> {
            // Parameters:
            // - double:           How many times should we loop
            // - CallableBinding:  The looped expressions

            if (args.length < 2) {
                return 0;
            }

            int n = Math.round(ValueConversions.asFloat(args[0]));
            Object expr = args[1];

            if (expr instanceof CallableBinding) {
                CallableBinding callable = (CallableBinding) expr;
                for (int i = 0; i < n; i++) {
                    callable.call();
                }
            }
            return 0;
        });
        o.block();
        return o;
    }

    public static ObjectBinding createQueryBinding(Supplier<PrintStream> stdout) {
        ObjectBinding o = new ObjectBinding();
        o.setProperty("print", (CallableBinding) (args) -> {
            int len = args.length;
            if (len > 0) {
                String[] strArgs = new String[len];
                for (int i = 0; i < len; i++) {
                    strArgs[i] = Objects.toString(args[i]);
                }
                stdout.get().println(String.join(" ", strArgs));
            }
            return 0;
        });
        o.block();
        return o;
    }

}
