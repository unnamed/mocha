package team.unnamed.molang.runtime;

import team.unnamed.molang.parser.ast.*;
import team.unnamed.molang.runtime.binding.CallableBinding;
import team.unnamed.molang.runtime.binding.ObjectBinding;
import team.unnamed.molang.runtime.binding.ValueConversions;

import java.util.List;

public class ExpressionEvaluator implements ExpressionVisitor<Object> {

    private static final Evaluator[] INFIX_EVALUATORS = {
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
            })
    };

    private final ObjectBinding bindings;
    private Object returnValue;

    public ExpressionEvaluator(ObjectBinding bindings) {
        this.bindings = bindings;
    }

    private ExpressionEvaluator createChild() {
        // Note that it will have its own returnValue, but same bindings
        // (Should we create new bindings?)
        return new ExpressionEvaluator(this.bindings);
    }

    @Override
    public Object visitAccess(AccessExpression expression) {
        Object binding = expression.object().visit(this);
        if (binding instanceof ObjectBinding) {
            return ((ObjectBinding) binding).getProperty(expression.property());
        }
        return null;
    }

    public Object popReturnValue() {
        Object val = this.returnValue;
        this.returnValue = null;
        return val;
    }

    @Override
    public Object visitAssign(AssignExpression expression) {
        Object val = expression.value().visit(this);
        Expression variable = expression.variable();
        if (variable instanceof AccessExpression) {
            AccessExpression access = (AccessExpression) variable;
            Object binding = access.object().visit(this);
            if (binding instanceof ObjectBinding) {
                ((ObjectBinding) binding).setProperty(access.property(), val);
            }
        }
        // TODO: (else case) This isn't fail-fast, we can only assign to access expressions
        return val;
    }

    @Override
    public Object visitCall(CallExpression expression) {
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
    public Object visitConditional(ConditionalExpression expression) {
        Object condition = expression.condition().visit(this);
        if (ValueConversions.asBoolean(condition)) {
            return expression.predicate().visit(this);
        } else {
            return 0;
        }
    }

    @Override
    public Object visitDouble(DoubleExpression expression) {
        return expression.value();
    }

    @Override
    public Object visitExecutionScope(ExecutionScopeExpression executionScope) {
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
    public Object visitIdentifier(IdentifierExpression expression) {
        return bindings.getProperty(expression.name());
    }

    @Override
    public Object visitInfix(InfixExpression expression) {
        return INFIX_EVALUATORS[expression.code()].eval(
                () -> expression.left().visit(this),
                () -> expression.right().visit(this)
        );
    }

    @Override
    public Object visitNegation(NegationExpression expression) {
        Object value = expression.expression().visit(this);
        if (value instanceof Boolean) {
            return !((boolean) value);
        } else if (value instanceof Number) {
            return -((Number) value).doubleValue();
        } else if (value == null) {
            // (value == null) = false
            // and then negate it = true = 0
            return 1.0D;
        } else {
            // non-null = true
            // and then negate it = false = 0
            return 0.0D;
        }
    }

    @Override
    public Object visitNullCoalescing(NullCoalescingExpression expression) {
        Object val = expression.value().visit(this);
        if (val == null) {
            return expression.fallback().visit(this);
        } else {
            return val;
        }
    }

    @Override
    public Object visitReturn(ReturnExpression expression) {
        this.returnValue = expression.value().visit(this);
        return 0;
    }

    @Override
    public Object visitString(StringExpression expression) {
        return expression.value();
    }

    @Override
    public Object visitTernaryConditional(TernaryConditionalExpression expression) {
        Object obj = expression.condition().visit(this);
        return ValueConversions.asBoolean(obj)
                ? expression.trueExpression().visit(this)
                : expression.falseExpression().visit(this);
    }

    @Override
    public Object visitWrapped(WrappedExpression expression) {
        return expression.expression().visit(this);
    }

    @Override
    public Object visit(Expression expression) {
        throw new UnsupportedOperationException("Unsupported expression type: " + expression);
    }

    private interface Evaluator {
        Object eval(LazyEvaluableObject a, LazyEvaluableObject b);
        interface LazyEvaluableObject {
            Object eval();
        }
    }

    private static Evaluator bool(BooleanOperator op) {
        return (a, b) -> op.operate(
                () -> {
                    Object obj = a.eval();
                    return ValueConversions.asBoolean(obj);
                },
                () -> {
                    Object obj = b.eval();
                    return ValueConversions.asBoolean(obj);
                }
        ) ? 1F : 0F;
    }

    private static Evaluator compare(Comparator comp) {
        return (a, b) -> comp.compare(
                () -> {
                    Object obj = a.eval();
                    return ValueConversions.asFloat(obj);
                },
                () -> {
                    Object obj = b.eval();
                    return ValueConversions.asFloat(obj);
                }
        ) ? 1F : 0F;
    }

    private static Evaluator arithmetic(ArithmeticOperator op) {
        return (a, b) -> op.operate(
                () -> {
                    Object obj = a.eval();
                    return ValueConversions.asFloat(obj);
                },
                () -> {
                    Object obj = b.eval();
                    return ValueConversions.asFloat(obj);
                }
        );
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
