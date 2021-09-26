package team.unnamed.molang.ast;

/**
 * Abstraction for all {@link Expression}
 * composed by two {@link Expression}, they
 * are the left-hand expression and the
 * right-hand expression, respectively
 */
public abstract class BinaryExpression
        implements Expression {

    protected final Expression leftHand;
    protected final Expression rightHand;

    public BinaryExpression(
            Expression leftHand,
            Expression rightHand
    ) {
        this.leftHand = leftHand;
        this.rightHand = rightHand;
    }

    public Expression getLeftHand() {
        return leftHand;
    }

    public Expression getRightHand() {
        return rightHand;
    }

}
