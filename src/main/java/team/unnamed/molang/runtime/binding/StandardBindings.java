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

import team.unnamed.molang.parser.ast.AccessExpression;
import team.unnamed.molang.parser.ast.Expression;
import team.unnamed.molang.parser.ast.StatementExpression;
import team.unnamed.molang.runtime.Function;

import java.io.PrintStream;
import java.util.Arrays;
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
        o.setProperty("loop", (Function) (ctx, args) -> {
            // Parameters:
            // - double:           How many times should we loop
            // - CallableBinding:  The looped expressions

            if (args.length < 2) {
                return 0;
            }

            int n = Math.round(ValueConversions.asFloat(args[0].eval()));
            Object expr = args[1].eval();

            if (expr instanceof Function) {
                Function callable = (Function) expr;
                for (int i = 0; i < n; i++) {
                    Object value = callable.evaluate(ctx);
                    if (value == StatementExpression.Op.BREAK) {
                        break;
                    }
                    // (not necessary, callable already exits when returnValue
                    //  is set to any non-null value)
                    // if (value == StatementExpression.Op.CONTINUE) continue;
                }
            }
            return 0;
        });
        o.setProperty("for_each", (Function) (ctx, args) -> {
            // Parameters:
            // - any:              Variable
            // - array:            Any array
            // - CallableBinding:  The looped expressions

            if (args.length < 3) {
                return 0;
            }

            final Expression variableExpr = args[0].expression();
            if (!(variableExpr instanceof AccessExpression)) {
                // first argument must be an access expression,
                // e.g. 'variable.test', 'v.pig', 't.entity' or
                // 't.entity.location.world'
                return 0;
            }
            final AccessExpression variableAccess = (AccessExpression) variableExpr;
            final Expression objectExpr = variableAccess.object();
            final String propertyName = variableAccess.property();

            final Object array = args[1].eval();
            final Iterable<?> arrayIterable;
            if (array instanceof Object[]) {
                arrayIterable = Arrays.asList((Object[]) array);
            } else if (array instanceof Iterable<?>) {
                arrayIterable = (Iterable<?>) array;
            } else {
                // second argument must be an array or iterable
                return 0;
            }

            final Object expr = args[2].eval();

            if (expr instanceof Function) {
                final Function callable = (Function) expr;
                for (final Object val : arrayIterable) {
                    // set 'val' as current value
                    // eval (objectExpr.propertyName = val)
                    final Object evaluatedObjectValue = objectExpr.visit(null);
                    if (evaluatedObjectValue instanceof ObjectBinding) {
                        ((ObjectBinding) evaluatedObjectValue).setProperty(propertyName, val);
                    }
                    final Object returnValue = callable.evaluate(ctx);

                    if (returnValue == StatementExpression.Op.BREAK) {
                        break;
                    }
                }
            }
            return 0;
        });
        o.block();
        return o;
    }

    public static ObjectBinding createQueryBinding(Supplier<PrintStream> stdout) {
        ObjectBinding o = new ObjectBinding();
        o.setProperty("print", (Function) (ctx, args) -> {
            int len = args.length;
            if (len > 0) {
                String[] strArgs = new String[len];
                for (int i = 0; i < len; i++) {
                    strArgs[i] = Objects.toString(args[i].eval());
                }
                stdout.get().println(String.join(" ", strArgs));
            }
            return 0;
        });
        o.block();
        return o;
    }

}
