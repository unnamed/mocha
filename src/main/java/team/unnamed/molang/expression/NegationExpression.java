package team.unnamed.molang.expression;

import team.unnamed.molang.context.EvalContext;

public class NegationExpression
        implements Expression {

    private final Expression expression;
    private final char token;

    public NegationExpression(Expression expression, char token) {
        this.expression = expression;
        this.token = token;
    }

    @Override
    public float evalAsFloat(EvalContext context) {
        return -expression.evalAsFloat(context);
    }

    @Override
    public boolean evalAsBoolean(EvalContext context) {
        return !expression.evalAsBoolean(context);
    }

    @Override
    public Object eval(EvalContext context) {
        return evalAsFloat(context);
    }

    @Override
    public String toString() {
        return (token + "") + expression;
    }
}
