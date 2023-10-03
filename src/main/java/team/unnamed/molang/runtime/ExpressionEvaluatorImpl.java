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

package team.unnamed.molang.runtime;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.molang.parser.ast.*;
import team.unnamed.molang.runtime.binding.CallableBinding;
import team.unnamed.molang.runtime.binding.ObjectBinding;
import team.unnamed.molang.runtime.binding.ValueConversions;

import java.util.List;
import java.util.Objects;

public final class ExpressionEvaluatorImpl implements ExpressionEvaluator {

    private static final Evaluator[] BINARY_EVALUATORS = {
            bool((a, b) -> a.eval() && b.eval()),
            bool((a, b) -> a.eval() || b.eval()),
            compare((a, b) -> a.eval() < b.eval()),
            compare((a, b) -> a.eval() <= b.eval()),
            compare((a, b) -> a.eval() > b.eval()),
            compare((a, b) -> a.eval() >= b.eval()),
            arithmetic((a, b) -> a.eval() + b.eval()),
            arithmetic((a, b) -> a.eval() - b.eval()),
            arithmetic((a, b) -> a.eval() * b.eval()),
            arithmetic((a, b) -> {
                // Molang allows division by zero,
                // which is always equal to 0
                float dividend = a.eval();
                float divisor = b.eval();
                if (divisor == 0) return 0;
                else return dividend / divisor;
            }),
            (evaluator, a, b) -> { // null coalesce
                Object val = a.visit(evaluator);
                if (val == null) {
                    return b.visit(evaluator);
                } else {
                    return val;
                }
            },
            (evaluator, a, b) -> { // assignation
                Object val = b.visit(evaluator);
                if (a instanceof AccessExpression) {
                    AccessExpression access = (AccessExpression) a;
                    Object binding = access.object().visit(evaluator);
                    if (binding instanceof ObjectBinding) {
                        ((ObjectBinding) binding).setProperty(access.property(), val);
                    }
                }
                // TODO: (else case) This isn't fail-fast, we can only assign to access expressions
                return val;
            },
            (evaluator, a, b) -> { // conditional
                Object condition = a.visit(evaluator);
                if (ValueConversions.asBoolean(condition)) {
                    return b.visit(evaluator);
                } else {
                    return 0;
                }
            },
            arithmetic((a, b) -> ((a.eval() == b.eval()) ? 1.0F : 0.0F)), // eq
            arithmetic((a, b) -> ((a.eval() != b.eval()) ? 1.0F : 0.0F))  // neq
    };

    private final ObjectBinding bindings;
    private @Nullable Object returnValue;

    public ExpressionEvaluatorImpl(final @NotNull ObjectBinding bindings) {
        this.bindings = Objects.requireNonNull(bindings, "bindings");
    }

    @Override
    public @NotNull ExpressionEvaluator createChild() {
        // Note that it will have its own returnValue, but same bindings
        // (Should we create new bindings?)
        return new ExpressionEvaluatorImpl(this.bindings);
    }

    @Override
    public @NotNull ObjectBinding bindings() {
        return bindings;
    }


    @Override
    public @Nullable Object popReturnValue() {
        Object val = this.returnValue;
        this.returnValue = null;
        return val;
    }

    @Override
    public Object visitAccess(@NotNull AccessExpression expression) {
        Object binding = expression.object().visit(this);
        if (binding instanceof ObjectBinding) {
            return ((ObjectBinding) binding).getProperty(expression.property());
        }
        return null;
    }

    @Override
    public Object visitCall(@NotNull CallExpression expression) {
        Object binding = expression.function().visit(this);
        if (!(binding instanceof CallableBinding)) {
            // TODO: This isn't fail-fast, check this in specification
            return 0;
        }

        List<Expression> arguments = expression.arguments();
        Object[] evaluatedArguments = new Object[arguments.size()];
        for (int i = 0; i < arguments.size(); i++) {
            evaluatedArguments[i] = arguments.get(i).visit(this);
        }
        return ((CallableBinding) binding).call(evaluatedArguments);
    }

    @Override
    public Object visitDouble(@NotNull DoubleExpression expression) {
        return expression.value();
    }

    @Override
    public Object visitExecutionScope(@NotNull ExecutionScopeExpression executionScope) {
        List<Expression> expressions = executionScope.expressions();
        ExpressionEvaluator evaluatorForThisScope = createChild();
        return (CallableBinding) arguments -> {
            for (Expression expression : expressions) {
                // eval expression, ignore result
                expression.visit(evaluatorForThisScope);

                // check for return values
                Object returnValue = evaluatorForThisScope.popReturnValue();
                if (returnValue != null) {
                    return returnValue;
                }
            }
            return 0;
        };
    }

    @Override
    public Object visitIdentifier(@NotNull IdentifierExpression expression) {
        return bindings.getProperty(expression.name());
    }

    @Override
    public Object visitBinary(@NotNull BinaryExpression expression) {
        return BINARY_EVALUATORS[expression.op().ordinal()].eval(
                this,
                expression.left(),
                expression.right()
        );
    }

    @Override
    public Object visitUnary(@NotNull UnaryExpression expression) {
        Object value = expression.expression().visit(this);
        switch (expression.op()) {
            case LOGICAL_NEGATION:
                return !ValueConversions.asBoolean(value);
            case ARITHMETICAL_NEGATION:
                return -ValueConversions.asFloat(value);
            case RETURN: {
                this.returnValue = value;
                return 0D;
            }
            default:
                throw new IllegalStateException("Unknown operation");
        }
    }

    @Override
    public Object visitStatement(@NotNull StatementExpression expression) {
        switch (expression.op()) {
            case BREAK: {
                this.returnValue = StatementExpression.Op.BREAK;
                break;
            }
            case CONTINUE: {
                this.returnValue = StatementExpression.Op.CONTINUE;
                break;
            }
        }
        return 0;
    }

    @Override
    public Object visitString(@NotNull StringExpression expression) {
        return expression.value();
    }

    @Override
    public Object visitTernaryConditional(@NotNull TernaryConditionalExpression expression) {
        Object obj = expression.condition().visit(this);
        return ValueConversions.asBoolean(obj)
                ? expression.trueExpression().visit(this)
                : expression.falseExpression().visit(this);
    }

    @Override
    public Object visit(@NotNull Expression expression) {
        throw new UnsupportedOperationException("Unsupported expression type: " + expression);
    }

    private static Evaluator bool(BooleanOperator op) {
        return (evaluator, a, b) -> op.operate(
                () -> ValueConversions.asBoolean(a.visit(evaluator)),
                () -> ValueConversions.asBoolean(b.visit(evaluator))
        ) ? 1F : 0F;
    }

    private static Evaluator compare(Comparator comp) {
        return (evaluator, a, b) -> comp.compare(
                () -> ValueConversions.asFloat(a.visit(evaluator)),
                () -> ValueConversions.asFloat(b.visit(evaluator))
        ) ? 1F : 0F;
    }

    private static Evaluator arithmetic(ArithmeticOperator op) {
        return (evaluator, a, b) -> op.operate(
                () -> ValueConversions.asFloat(a.visit(evaluator)),
                () -> ValueConversions.asFloat(b.visit(evaluator))
        );
    }

    private interface Evaluator {
        Object eval(ExpressionEvaluator evaluator, Expression a, Expression b);
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


}
