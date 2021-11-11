package team.unnamed.molang.ast.binary;

import team.unnamed.molang.ast.Expression;
import team.unnamed.molang.context.EvalContext;

public class ModifyExpression implements Expression {

    private final Expression variable;
    private final Expression value;

    public ModifyExpression(
            Expression variable,
            Expression value
    ) {
        this.variable = variable;
        this.value = value;
    }

    @Override
    public Object eval(EvalContext context) {
        Object val = value.eval(context);
        variable.setValue(context, val);
        return val;
    }

    @Override
    public String toSource() {
        return variable.toSource() + " = " + value.toSource();
    }

    @Override
    public String toString() {
        return "Assign(" + variable + ", " + value + ")";
    }

}
