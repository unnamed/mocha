package team.unnamed.molang.expression.binary.logical;

import team.unnamed.molang.context.EvalContext;
import team.unnamed.molang.expression.Expression;
import team.unnamed.molang.expression.binary.BinaryExpression;

/**
 * A logical "AND" expression, with a left-hand
 * and right-hand expressions that will be compared
 * to get the final result
 */
public class AndExpression
        extends BinaryExpression {

    public AndExpression(Expression leftHand, Expression rightHand) {
        super(leftHand, rightHand);
    }

    @Override
    public Class<?> getType() {
        return Object.class;
    }

    @Override
    public Object eval(EvalContext context) {
        return leftHand.evalAsBoolean(context)
                && rightHand.evalAsBoolean(context);
    }

    @Override
    public String toString() {
        return leftHand + " && " + rightHand;
    }

}
