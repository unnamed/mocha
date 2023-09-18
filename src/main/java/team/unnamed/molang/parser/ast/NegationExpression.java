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
        return "Negate(" + expression + ")";
    }

}
