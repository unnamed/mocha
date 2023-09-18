package team.unnamed.molang.parser.ast;

import team.unnamed.molang.runtime.EvalContext;

/**
 * The null coalescing expression implementation,
 * if the result of evaluating the 'leftHand' expression
 * is considered invalid, then it returns the 'rightHand'
 * result
 */
public final class NullCoalescingExpression implements Expression {

    private final Expression value;
    private final Expression fallback;

    public NullCoalescingExpression(Expression value, Expression fallback) {
        this.value = value;
        this.fallback = fallback;
    }

    public Expression value() {
        return value;
    }

    public Expression fallback() {
        return fallback;
    }

    @Override
    public <R> R visit(ExpressionVisitor<R> visitor) {
        return visitor.visitNullCoalescing(this);
    }

    @Override
    public String toSource() {
        return value.toSource() + " ?? "
                + fallback.toSource();
    }

    @Override
    public String toString() {
        return "NullCoalescing(" + value + ", " + fallback + ")";
    }

}
