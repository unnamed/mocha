package team.unnamed.molang.runtime;

import team.unnamed.molang.parser.ast.*;
import team.unnamed.molang.runtime.binding.CallableBinding;
import team.unnamed.molang.runtime.binding.ObjectBinding;

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

    private final EvalContext context;

    public ExpressionEvaluator(EvalContext context) {
        this.context = context;
    }

    @Override
    public Object visitAccess(AccessExpression expression) {
        Object binding = expression.object().visit(this);
        if (binding instanceof ObjectBinding) {
            return ((ObjectBinding) binding).getProperty(expression.property());
        }
        return null;
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
        if (asBoolean(condition)) {
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
        for (Expression expression : expressions) {
            // eval expression, ignore result
            expression.visit(this);

            // check for return values
            Object returnValue = context.popReturnValue();
            if (returnValue != null) {
                return returnValue;
            }
        }
        return 0;
    }

    @Override
    public Object visitIdentifier(IdentifierExpression expression) {
        return context.getBinding(expression.name());
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
        Object value = expression.value().visit(this);
        // set the scope return value
        context.setReturnValue(value);
        return 0;
    }

    @Override
    public Object visitString(StringExpression expression) {
        return expression.value();
    }

    @Override
    public Object visitTernaryConditional(TernaryConditionalExpression expression) {
        return asBoolean(expression.condition().visit(this))
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

    private static boolean asBoolean(Object obj) {
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        } else if (obj instanceof Number) {
            // '0' is considered false here, anything else
            // is considered true.
            return ((Number) obj).floatValue() != 0;
        } else {
            return true;
        }
    }

    private static float asFloat(Object obj) {
        if (obj instanceof Boolean) {
            return ((Boolean) obj) ? 1 : 0;
        } else if (!(obj instanceof Number)) {
            return 0;
        } else {
            return ((Number) obj).floatValue();
        }
    }

    private interface Evaluator {
        Object eval(LazyEvaluableObject a, LazyEvaluableObject b);
        interface LazyEvaluableObject {
            Object eval();
        }
    }

    private static Evaluator bool(BooleanOperator op) {
        return (a, b) -> op.operate(
                () -> asBoolean(a.eval()),
                () -> asBoolean(b.eval())
        ) ? 1F : 0F;
    }

    private static Evaluator compare(Comparator comp) {
        return (a, b) -> comp.compare(
                () -> asFloat(a.eval()),
                () -> asFloat(b.eval())
        ) ? 1F : 0F;
    }

    private static Evaluator arithmetic(ArithmeticOperator op) {
        return (a, b) -> op.operate(
                () -> asFloat(a.eval()),
                () -> asFloat(b.eval())
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
