package team.unnamed.molang.parser.ast;

/**
 * Implementation of MoLang 1.17 binary conditional
 * expression, it's similar to an "if {...} " expression.
 *
 * See https://bedrock.dev/docs/1.17.0.0/1.17.30.4/Molang#Conditionals
 *
 * <p>If the evaluated value of the {@code conditional}
 * expression is considered true, it evaluates the
 * {@code predicate} expression.</p>
 */
public class ConditionalExpression implements Expression {

    private final Expression condition;
    private final Expression predicate;

    public ConditionalExpression(Expression condition, Expression predicate) {
        this.condition = condition;
        this.predicate = predicate;
    }

    public Expression condition() {
        return condition;
    }

    public Expression predicate() {
        return predicate;
    }

    @Override
    public <R> R visit(ExpressionVisitor<R> visitor) {
        return visitor.visitConditional(this);
    }

    @Override
    public String toSource() {
        return condition.toSource() + " ? " + predicate.toSource();
    }

    @Override
    public String toString() {
        return "Condition(" + condition + ", " + predicate + ")";
    }

}
