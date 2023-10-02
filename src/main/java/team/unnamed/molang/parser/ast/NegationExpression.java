package team.unnamed.molang.parser.ast;

/**
 * Expression implementation for the MoLang 1.17
 * negation expression, it may negate numbers or
 * boolean expressions
 */
public class NegationExpression implements Expression {

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
    public Expression expression() {
        return expression;
    }

    @Override
    public <R> R visit(ExpressionVisitor<R> visitor) {
        return visitor.visitNegation(this);
    }

    @Override
    public String toSource() {
        return token + expression.toSource();
    }

    @Override
    public String toString() {
        return "Negation(" + expression + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NegationExpression that = (NegationExpression) o;
        if (token != that.token) return false;
        return expression.equals(that.expression);
    }

    @Override
    public int hashCode() {
        int result = expression.hashCode();
        result = 31 * result + (int) token;
        return result;
    }
}
