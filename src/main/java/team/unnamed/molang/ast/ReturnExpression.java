package team.unnamed.molang.ast;

import team.unnamed.molang.context.EvalContext;

/**
 * Expression implementation of MoLang 1.17
 * return statements
 */
public class ReturnExpression implements Expression {

    private final Expression value;

    public ReturnExpression(Expression value) {
        this.value = value;
    }

    /**
     * Returns the expression indicating
     * the return value of this {@code return}
     * statement
     */
    public Expression getValue() {
        return value;
    }

    @Override
    public Object eval(EvalContext context) {
        Object value = this.value.eval(context);
        // set the scope return value
        context.setReturnValue(value);
        return 0;
    }

    @Override
    public String toSource() {
        return "return " + this.value.toSource();
    }

    @Override
    public String toString() {
        return "Return(" + this.value + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReturnExpression that = (ReturnExpression) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
