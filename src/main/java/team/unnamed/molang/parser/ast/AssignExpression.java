package team.unnamed.molang.parser.ast;

public class AssignExpression implements Expression {

    private final Expression variable;
    private final Expression value;

    public AssignExpression(
            Expression variable,
            Expression value
    ) {
        this.variable = variable;
        this.value = value;
    }

    public Expression variable() {
        return variable;
    }

    public Expression value() {
        return value;
    }

    @Override
    public <R> R visit(ExpressionVisitor<R> visitor) {
        return visitor.visitAssign(this);
    }

    @Override
    public String toSource() {
        return variable.toSource() + " = " + value.toSource();
    }

    @Override
    public String toString() {
        return "Assign(" + variable + ", " + value + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssignExpression that = (AssignExpression) o;
        if (!variable.equals(that.variable)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        int result = variable.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

}
