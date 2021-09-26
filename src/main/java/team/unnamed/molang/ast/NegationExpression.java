package team.unnamed.molang.ast;

import team.unnamed.molang.context.EvalContext;

/**
 * Expression implementation for the MoLang 1.17
 * negation expression, it may negate numbers or
 * boolean expressions
 */
public class NegationExpression
        implements Expression {

    private final Expression expression;
    private final char token;

    public NegationExpression(Expression expression, char token) {
        this.expression = expression;
        this.token = token;
    }

    /**
     * Returns the negated expression
     * @return The negated expression,
     * never null
     */
    public Expression getExpression() {
        return expression;
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
    public String toSource() {
        return token + expression.toSource();
    }

    @Override
    public String toString() {
        return "Negate(" + expression + ")";
    }

}
