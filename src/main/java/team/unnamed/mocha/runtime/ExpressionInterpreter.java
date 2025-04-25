/*
 * This file is part of mocha, licensed under the MIT license
 *
 * Copyright (c) 2021-2025 Unnamed Team
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
package team.unnamed.mocha.runtime;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mocha.parser.ast.*;
import team.unnamed.mocha.runtime.binding.JavaFunction;
import team.unnamed.mocha.runtime.value.*;

import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;

@ApiStatus.Internal
public final class ExpressionInterpreter<T> implements ExpressionVisitor<Value>, ExecutionContext<T> {
    private static final List<Evaluator> BINARY_EVALUATORS = Arrays.asList(
            bool((a, b) -> a.eval() && b.eval()),
            bool((a, b) -> a.eval() || b.eval()),
            compare((a, b) -> a.eval() < b.eval()),
            compare((a, b) -> a.eval() <= b.eval()),
            compare((a, b) -> a.eval() > b.eval()),
            compare((a, b) -> a.eval() >= b.eval()),
            (evaluator, a, b) -> {
                final Value aVal = a.visit(evaluator);
                final Value bVal = b.visit(evaluator);
                // string concatenation is not supported in molang
                // if (aVal.isString() || bVal.isString()) {
                //     return StringValue.of(aVal.getAsString() + bVal.getAsString());
                // } else {
                return NumberValue.of(aVal.getAsNumber() + bVal.getAsNumber());
                // }
            },
            arithmetic((a, b) -> a.eval() - b.eval()),
            arithmetic((a, b) -> a.eval() * b.eval()),
            arithmetic((a, b) -> {
                // Molang allows division by zero,
                // which is always equal to 0
                final float dividend = a.eval();
                final float divisor = b.eval();
                if (divisor == 0) return 0;
                else return dividend / divisor;
            }),
            (evaluator, a, b) -> { // arrow
                final Value val = a.visit(evaluator);
                if (!(val instanceof JavaValue)) {
                    return NumberValue.zero();
                } else {
                    return b.visit(evaluator.createChild(((JavaValue) val).value()));
                }
            },
            (evaluator, a, b) -> { // null coalesce
                final Value val = a.visit(evaluator);
                if (val.getAsBoolean()) {
                    return val;
                } else {
                    return b.visit(evaluator);
                }
            },
            (evaluator, a, b) -> { // assignation
                final Value val = b.visit(evaluator);
                // we can only assign to values that are accessed
                // like:
                //      temp.x = 1
                //      t.location.world = 'world'
                // but not:
                //      x = 1
                //      i = 2
                if (a instanceof AccessExpression) {
                    final AccessExpression access = (AccessExpression) a;
                    final Value objectValue = access.object().visit(evaluator);
                    if (objectValue instanceof MutableObjectBinding) {
                        ((MutableObjectBinding) objectValue).set(access.property(), val);
                    }
                }
                return val;
            },
            (evaluator, a, b) -> { // conditional
                final Value conditionValue = a.visit(evaluator);
                if (conditionValue.getAsBoolean()) {
                    final Value predicateVal = b.visit(evaluator);
                    if (predicateVal instanceof Function) {
                        return Value.of(((Function) predicateVal).evaluate(evaluator));
                    } else {
                        return predicateVal;
                    }
                }
                return NumberValue.zero();
            },
            arithmetic((a, b) -> ((a.eval() == b.eval()) ? 1.0F : 0.0F)), // eq
            arithmetic((a, b) -> ((a.eval() != b.eval()) ? 1.0F : 0.0F))  // neq
    );

    private final T entity;
    private final Scope scope;
    private @Nullable Object flag;
    private @Nullable Value returnValue;

    private boolean warnOnReflectiveFunctionUsage;

    public ExpressionInterpreter(final @Nullable T entity, final @NotNull Scope scope) {
        this.entity = entity;
        this.scope = requireNonNull(scope, "scope");
    }

    private static Evaluator bool(BooleanOperator op) {
        return (evaluator, a, b) -> Value.of(op.operate(
                () -> a.visit(evaluator).getAsBoolean(),
                () -> b.visit(evaluator).getAsBoolean()
        ));
    }

    private static Evaluator compare(Comparator comp) {
        return (evaluator, a, b) -> Value.of(comp.compare(
                () -> a.visit(evaluator).getAsNumber(),
                () -> b.visit(evaluator).getAsNumber()
        ));
    }

    private static Evaluator arithmetic(ArithmeticOperator op) {
        return (evaluator, a, b) -> NumberValue.of(op.operate(
                () -> a.visit(evaluator).getAsNumber(),
                () -> b.visit(evaluator).getAsNumber()
        ));
    }

    public void warnOnReflectiveFunctionUsage(final boolean warnOnReflectiveFunctionUsage) {
        this.warnOnReflectiveFunctionUsage = warnOnReflectiveFunctionUsage;
    }

    @Override
    public @Nullable Object flag() {
        return flag;
    }

    @Override
    public void flag(final @Nullable Object flag) {
        this.flag = flag;
    }

    @Override
    public T entity() {
        return entity;
    }

    @Override
    public @NotNull Value eval(final @NotNull Expression expression) {
        return expression.visit(this);
    }

    public <R> @NotNull ExpressionInterpreter<R> createChild(final @Nullable R entity) {
        return new ExpressionInterpreter<>(entity, this.scope);
    }

    public @NotNull ExpressionInterpreter<T> createChild() {
        // Note that it will have its own returnValue, but same bindings
        // (Should we create new bindings?)
        return new ExpressionInterpreter<>(this.entity, this.scope);
    }

    public @NotNull Scope bindings() {
        return scope;
    }

    public @Nullable Value popReturnValue() {
        final Value val = this.returnValue;
        this.returnValue = null;
        return val;
    }

    @Override
    public @NotNull Value visitArrayAccess(final @NotNull ArrayAccessExpression expression) {
        final Value array = expression.array().visit(this);
        final Value index = expression.index().visit(this);
        if (!(array instanceof ArrayValue)) {
            return Value.nil();
        } else {
            final Value[] values = ((ArrayValue) array).values();
            final int validIndex = Math.max(0, (int) index.getAsNumber()) % values.length;
            return values[validIndex];
        }
    }

    @Override
    public @NotNull Value visitAccess(final @NotNull AccessExpression expression) {
        final Value objectValue = expression.object().visit(this);
        if (objectValue instanceof ObjectValue) {
            return ((ObjectValue) objectValue).get(expression.property());
        }
        return NumberValue.zero();
    }

    @Override
    public @NotNull Value visitCall(final @NotNull CallExpression expression) {
        final List<Expression> argumentsExpressions = expression.arguments();
        final Function.Argument[] arguments = new Function.Argument[argumentsExpressions.size()];
        for (int i = 0; i < argumentsExpressions.size(); i++) {
            arguments[i] = new FunctionArgumentImpl(argumentsExpressions.get(i));
        }
        final Function.Arguments args = new FunctionArguments(arguments);

        final Expression functionExpr = expression.function();
        if (functionExpr instanceof IdentifierExpression) {
            final String identifierName = ((IdentifierExpression) functionExpr).name();
            if ("loop".equals(identifierName)) {
                // loop built-in function
                // Parameters:
                // - float:           How many times should we loop
                // - CallableBinding:  The looped expressions
                int n = Math.round(args.next().eval().getAsNumber());
                Value expr = args.next().eval();

                if (expr instanceof Function) {
                    final Function<T> callable = (Function<T>) expr;
                    for (int i = 0; i < n; i++) {
                        final ExpressionInterpreter<T> evaluatorThisCall = createChild();
                        callable.evaluate(evaluatorThisCall);
                        if (evaluatorThisCall.flag() == StatementExpression.Op.BREAK) {
                            break;
                        }
                        // (not necessary, callable already exits when returnValue
                        //  is set to any non-null value)
                        // if (value == StatementExpression.Op.CONTINUE) continue;
                    }
                }
                return NumberValue.zero();
            } else if ("for_each".equals(identifierName)) {
                // for each built-in function
                // Parameters:
                // - any:              Variable
                // - array:            Any array
                // - CallableBinding:  The looped expressions
                final Expression variableExpr = args.next().expression();
                if (!(variableExpr instanceof AccessExpression)) {
                    // first argument must be an access expression,
                    // e.g. 'variable.test', 'v.pig', 't.entity' or
                    // 't.entity.location.world'
                    return NumberValue.zero();
                }
                final AccessExpression variableAccess = (AccessExpression) variableExpr;
                final Expression objectExpr = variableAccess.object();
                final String propertyName = variableAccess.property();

                final Value array = args.next().eval();
                final Iterable<Value> arrayIterable;
                if (array instanceof ArrayValue) {
                    arrayIterable = Arrays.asList(((ArrayValue) array).values());
                } else {
                    // second argument must be an array or iterable
                    return NumberValue.zero();
                }

                final Value expr = args.next().eval();

                if (expr instanceof Function) {
                    final Function callable = (Function) expr;
                    for (final Value val : arrayIterable) {
                        // set 'val' as current value
                        // eval (objectExpr.propertyName = val)
                        final Value evaluatedObjectValue = this.eval(objectExpr);
                        if (evaluatedObjectValue instanceof MutableObjectBinding) {
                            ((MutableObjectBinding) evaluatedObjectValue).set(propertyName, val);
                        }
                        final Object returnValue = callable.evaluate(this);

                        if (returnValue == StatementExpression.Op.BREAK) {
                            break;
                        }
                    }
                }
                return NumberValue.zero();
            }
        }

        final Value function = functionExpr.visit(this);
        if (!(function instanceof Function)) {
            return Value.nil();
        }

        if (warnOnReflectiveFunctionUsage && function instanceof JavaFunction) {
            final JavaFunction<?> javaFunction = (JavaFunction<?>) function;
            System.err.println("Warning: Reflective function usage detected for method: " + javaFunction.method());
        }

        return ((Function<T>) function).evaluate(this, args);
    }

    @Override
    public @NotNull Value visitFloat(final @NotNull FloatExpression expression) {
        return NumberValue.of(expression.value());
    }

    @Override
    public @NotNull Value visitExecutionScope(final @NotNull ExecutionScopeExpression executionScope) {
        List<Expression> expressions = executionScope.expressions();
        return (Function<T>) (context, arguments) -> {
            for (Expression expression : expressions) {
                // eval expression, ignore result
                context.eval(expression);

                // check for return values
                if (context.flag() != null) {
                    break;
                }
            }
            return NumberValue.zero();
        };
    }

    @Override
    public @NotNull Value visitIdentifier(final @NotNull IdentifierExpression expression) {
        return scope.get(expression.name());
    }

    @Override
    public @NotNull Value visitBinary(@NotNull BinaryExpression expression) {
        return BINARY_EVALUATORS.get(expression.op().ordinal()).eval(
                this,
                expression.left(),
                expression.right()
        );
    }

    @Override
    public @NotNull Value visitUnary(final @NotNull UnaryExpression expression) {
        final Value value = expression.expression().visit(this);
        switch (expression.op()) {
            case LOGICAL_NEGATION:
                return Value.of(!value.getAsBoolean());
            case ARITHMETICAL_NEGATION:
                return NumberValue.of(-value.getAsNumber());
            case RETURN: {
                this.returnValue = value;
                return NumberValue.zero();
            }
            default:
                throw new IllegalStateException("Unknown operation");
        }
    }

    @Override
    public @NotNull Value visitStatement(final @NotNull StatementExpression expression) {
        switch (expression.op()) {
            case BREAK: {
                this.flag = StatementExpression.Op.BREAK;
                break;
            }
            case CONTINUE: {
                this.flag = StatementExpression.Op.CONTINUE;
                break;
            }
        }
        return NumberValue.zero();
    }

    @Override
    public @NotNull Value visitString(final @NotNull StringExpression expression) {
        return StringValue.of(expression.value());
    }

    @Override
    public @NotNull Value visitTernaryConditional(@NotNull TernaryConditionalExpression expression) {
        final Value conditionResult = expression.condition().visit(this);
        return conditionResult.getAsBoolean()
                ? expression.trueExpression().visit(this)
                : expression.falseExpression().visit(this);
    }

    @Override
    public Value visit(final @NotNull Expression expression) {
        throw new UnsupportedOperationException("Unsupported expression type: " + expression);
    }

    private interface Evaluator {
        @NotNull Value eval(ExpressionInterpreter<?> evaluator, Expression a, Expression b);
    }

    private interface BooleanOperator {
        boolean operate(LazyEvaluableBoolean a, LazyEvaluableBoolean b);
    }

    interface LazyEvaluableBoolean {
        boolean eval();
    }

    interface LazyEvaluableFloat {
        float eval();
    }

    private interface Comparator {
        boolean compare(LazyEvaluableFloat a, LazyEvaluableFloat b);

    }

    private interface ArithmeticOperator {
        float operate(LazyEvaluableFloat a, LazyEvaluableFloat b);
    }

    public static class FunctionArguments implements Function.Arguments {
        public static final Function.Arguments EMPTY = new FunctionArguments(new Function.Argument[0]);

        private final Function.Argument[] arguments;
        private int next;

        FunctionArguments(final @NotNull Function.Argument @NotNull [] arguments) {
            this.arguments = requireNonNull(arguments, "arguments");
        }

        @Override
        public Function.@NotNull Argument next() {
            if (next < arguments.length) {
                return arguments[next++];
            } else {
                return EmptyFunctionArgument.EMPTY;
            }
        }

        @Override
        public int length() {
            return arguments.length;
        }
    }

    private static class EmptyFunctionArgument implements Function.Argument {
        static final Function.Argument EMPTY = new EmptyFunctionArgument();

        @Override
        public @Nullable Expression expression() {
            return null;
        }

        @Override
        public @Nullable Value eval() {
            return NumberValue.zero();
        }
    }

    private class FunctionArgumentImpl implements Function.Argument {
        private final Expression expression;

        FunctionArgumentImpl(final @NotNull Expression expression) {
            this.expression = expression;
        }

        @Override
        public @NotNull Expression expression() {
            return expression;
        }

        @Override
        public @Nullable Value eval() {
            return expression.visit(ExpressionInterpreter.this);
        }
    }
}
