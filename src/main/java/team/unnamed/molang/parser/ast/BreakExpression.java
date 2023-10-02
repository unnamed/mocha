package team.unnamed.molang.parser.ast;

public class BreakExpression implements Expression {

    public static final Object BREAK_FLAG = new Object();
    
    @Override
    public <R> R visit(ExpressionVisitor<R> visitor) {
        return visitor.visitBreak(this);
    }

    @Override
    public String toSource() {
        return "break";
    }

    @Override
    public String toString() {
        return "Break";
    }

}
