package team.unnamed.molang.ast;

import team.unnamed.molang.context.EvalContext;

/**
 * {@link InfixExpression} implementation for
 * representing field accessing
 */
public class AccessExpression
        extends InfixExpression {

    public AccessExpression(
            Expression leftHand,
            Expression rightHand
    ) {
        super(leftHand, rightHand);
    }

    @Override
    public Object eval(EvalContext context) {
        return leftHand.evalProperty(context, rightHand); // temporary
    }

    @Override
    public String toSource() {
        return leftHand.toSource() + '.' + rightHand.toSource();
    }

    @Override
    public String toString() {
        return "Access(" + leftHand + ", " + rightHand + ")";
    }

}
