package team.unnamed.molang.ast.logical;

import team.unnamed.molang.ast.BooleanExpression;
import team.unnamed.molang.ast.Expression;
import team.unnamed.molang.ast.InfixExpression;
import team.unnamed.molang.context.EvalContext;

/**
 * A logical "AND" expression, with a left-hand
 * and right-hand expressions that will be compared
 * to get the final result
 */
public class AndExpression
        extends InfixExpression
        implements BooleanExpression {

    public AndExpression(Expression leftHand, Expression rightHand) {
        super(leftHand, rightHand);
    }

    @Override
    public boolean evalAsBoolean(EvalContext context) {
        return leftHand.evalAsBoolean(context)
                && rightHand.evalAsBoolean(context);
    }

    @Override
    public String toSource() {
        return leftHand.toSource() + " && " + rightHand.toSource();
    }

    @Override
    public String toString() {
        return "And(" + leftHand + ", " + rightHand + ")";
    }

}
