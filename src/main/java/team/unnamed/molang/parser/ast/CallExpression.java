package team.unnamed.molang.parser.ast;

import java.util.Iterator;
import java.util.List;

/**
 * Expression implementation for MoLang 1.17 function
 * call expression
 */
public final class CallExpression implements Expression {

    private final Expression function;
    private final List<Expression> arguments;

    public CallExpression(Expression function, List<Expression> arguments) {
        this.function = function;
        this.arguments = arguments;
    }

    /**
     * Returns the expression evaluated to the
     * invoked function
     */
    public Expression function() {
        return function;
    }

    /**
     * Returns the expressions evaluated to the
     * function arguments
     */
    public List<Expression> arguments() {
        return arguments;
    }

    @Override
    public <R> R visit(ExpressionVisitor<R> visitor) {
        return visitor.visitCall(this);
    }

    @Override
    public String toSource() {
        StringBuilder builder = new StringBuilder()
                .append(function)
                .append('(');

        Iterator<Expression> argIterator = arguments.iterator();

        while (argIterator.hasNext()) {
            Expression argument = argIterator.next();
            builder.append(argument);

            if (argIterator.hasNext()) {
                builder.append(", ");
            }
        }

        return builder.append(')').toString();
    }

    @Override
    public String toString() {
        return "Call(" + function + ", " + arguments + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CallExpression that = (CallExpression) o;
        if (!function.equals(that.function)) return false;
        return arguments.equals(that.arguments);
    }

    @Override
    public int hashCode() {
        int result = function.hashCode();
        result = 31 * result + arguments.hashCode();
        return result;
    }
}
