package team.unnamed.molang.ast.binary.logical;

import team.unnamed.molang.context.EvalContext;
import team.unnamed.molang.ast.Expression;
import team.unnamed.molang.ast.binary.BinaryExpression;

/**
 * A logical "OR" expression, with a left-hand
 * and right-hand expressions that will be compared
 * to get the final result
 */
public class OrExpression
        extends BinaryExpression {

    public OrExpression(Expression leftHand, Expression rightHand) {
        super(leftHand, rightHand);
    }

    @Override
    public Object eval(EvalContext context) {
        return leftHand.evalAsBoolean(context) || rightHand.evalAsBoolean(context);
    }

    @Override
    public String toString() {
        return leftHand + " || " + rightHand;
    }
}
