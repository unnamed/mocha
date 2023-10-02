/*
 * This file is part of molang, licensed under the MIT license
 *
 * Copyright (c) 2021-2023 Unnamed Team
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

package team.unnamed.molang.runtime.binding;

import team.unnamed.molang.parser.ast.BreakExpression;

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
                    Object value = callable.call();
                    if (value == BreakExpression.BREAK_FLAG) {
                        break;
                    }
                    // (not necessary, callable already exits when returnValue
                    //  is set to any non-null value)
                    // if (value == CONTINUE_FLAG) continue;
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
