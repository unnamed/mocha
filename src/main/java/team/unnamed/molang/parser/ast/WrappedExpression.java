package team.unnamed.molang.parser.ast;

import team.unnamed.molang.runtime.EvalContext;

/**
 * Expression implementation of a simple expression
 * wrapped inside parenthesis to delimit the expression,
 * it simply delegates all behavior to wrapped expression
 */
public class WrappedExpression implements Expression {

    private final Expression expression;

    public WrappedExpression(Expression expression) {
        this.expression = expression;
    }

    /**
     * Returns the wrapped expression
     * @return The wrapped expression,
     * never null
     */
    public Expression getExpression() {
        return expression;
    }

    @Override
    public Object eval(EvalContext context) {
        return expression.eval(context);
    }

    @Override
    public float evalAsFloat(EvalContext context) {
        return expression.evalAsFloat(context);
    }

    @Override
    public String toSource() {
        return "(" + expression.toSource() + ")";
    }

    @Override
    public String toString() {
        return expression.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WrappedExpression that = (WrappedExpression) o;
        return expression.equals(that.expression);
    }

    @Override
    public int hashCode() {
        return expression.hashCode();
    }

}
