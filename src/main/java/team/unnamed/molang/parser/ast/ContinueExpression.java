package team.unnamed.molang.parser.ast;

public class ContinueExpression implements Expression {

    public static final Object CONTINUE_FLAG = new Object();

    @Override
    public <R> R visit(ExpressionVisitor<R> visitor) {
        return visitor.visitContinue(this);
    }

    @Override
    public String toSource() {
        return "continue";
    }

    @Override
    public String toString() {
        return "Continue";
    }

}
