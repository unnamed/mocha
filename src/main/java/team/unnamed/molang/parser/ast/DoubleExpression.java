package team.unnamed.molang.parser.ast;

import java.util.Objects;

/**
 * Literal expression implementation for MoLang 1.17
 * numerical values
 * See https://bedrock.dev/docs/1.17.0.0/1.17.30.4/Molang#Values
 */
public class DoubleExpression implements Expression {

    private final double value;

    public DoubleExpression(double value) {
        this.value = value;
    }

    /**
     * Returns the value of this double
     * expression
     */
    public double value() {
        return value;
    }

    @Override
    public <R> R visit(ExpressionVisitor<R> visitor) {
        return visitor.visitDouble(this);
    }

    @Override
    public String toSource() {
        return Double.toString(value);
    }

    @Override
    public String toString() {
        return "Double(" + value + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DoubleExpression that = (DoubleExpression) o;
        return Double.compare(that.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

}
