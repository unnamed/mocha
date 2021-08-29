package team.unnamed.molang.expression;

import team.unnamed.molang.context.EvalContext;

import java.util.List;

public class WrappedExpression implements Expression {

    private final Expression expression;

    public WrappedExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public Object eval(EvalContext context) {
        return expression.eval(context);
    }

    @Override
    public Object evalProperty(EvalContext context, Expression property) {
        return expression.evalProperty(context, property);
    }

    @Override
    public Object call(EvalContext context, List<Expression> arguments) {
        return expression.call(context, arguments);
    }

    @Override
    public float evalAsFloat(EvalContext context) {
        return expression.evalAsFloat(context);
    }

    @Override
    public String toString() {
        return "(" + expression + ")";
    }
}
