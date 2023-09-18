package team.unnamed.molang.parser.ast;

import team.unnamed.molang.lexer.Tokens;

/**
 * {@link Expression} implementation for
 * representing property accessing
 */
public class AccessExpression implements Expression {

    private final Expression object;
    private final String property;

    public AccessExpression(
            Expression object,
            String property
    ) {
        this.object = object;
        this.property = property;
    }

    public Expression object() {
        return object;
    }

    public String property() {
        return property;
    }

    @Override
    public <R> R visit(ExpressionVisitor<R> visitor) {
        return visitor.visitAccess(this);
    }

    @Override
    public String toSource() {
        return object.toSource() + Tokens.DOT + property;
    }

    @Override
    public String toString() {
        return "Access(" + object + ", " + property + ")";
    }

}
