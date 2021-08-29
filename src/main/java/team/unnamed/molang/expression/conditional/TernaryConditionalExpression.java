package team.unnamed.molang.expression.conditional;

import team.unnamed.molang.context.EvalContext;
import team.unnamed.molang.expression.Expression;

/**
 * Implementation of the ternary conditional expression,
 * it's similar to an "if {...} else {...}" expression.
 *
 * Depending on the conditional expression, it may
 * return the {@code trueExpression} or the {@code falseExpression}
 */
public class TernaryConditionalExpression implements Expression {

    private final Expression conditional;
    private final Expression trueExpression;
    private final Expression falseExpression;

    public TernaryConditionalExpression(
            Expression conditional,
            Expression trueExpression,
            Expression falseExpression
    ) {
        this.conditional = conditional;
        this.trueExpression = trueExpression;
        this.falseExpression = falseExpression;
    }

    @Override
    public Object eval(EvalContext context) {
        return conditional.evalAsBoolean(context)
                ? trueExpression.eval(context)
                : falseExpression.eval(context);
    }

    @Override
    public String toString() {
        return conditional
                + " ? " + trueExpression
                + " : " + falseExpression;
    }

}
