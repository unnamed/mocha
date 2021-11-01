package team.unnamed.molang.ast;

import team.unnamed.molang.context.EvalContext;

/**
 * Abstraction for any boolean expression (can return
 * true or false), converts the boolean to a float 1
 * if true, or 0 if false
 */
public interface BooleanExpression extends Expression {

    @Override
    boolean evalAsBoolean(EvalContext context);

    @Override
    default Object eval(EvalContext context) {
        return evalAsBoolean(context) ? 1F : 0F;
    }

}
