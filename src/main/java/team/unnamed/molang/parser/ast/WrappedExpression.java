package team.unnamed.molang.parser.ast;

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
    public Expression expression() {
        return expression;
    }

    @Override
    public <R> R visit(ExpressionVisitor<R> visitor) {
        return visitor.visitWrapped(this);
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
