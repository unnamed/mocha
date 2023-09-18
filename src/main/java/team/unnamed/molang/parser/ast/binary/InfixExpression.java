package team.unnamed.molang.parser.ast.binary;

import team.unnamed.molang.parser.ast.Expression;
import team.unnamed.molang.runtime.EvalContext;

/**
 * Represents any binary expression that operates two
 * expressions, they can be arithmetic or boolean
 */
public class InfixExpression implements Expression {

    private static final String[] NAMES = {
            "And", "Or", "LessThan", "LessThanOrEqual", "GreaterThan", "GreaterThanOrEqual",
            "Add", "Subtract", "Multiply", "Divide"
    };
    private static final String[] SYMBOLS = {
            "&&", "||", "<", "<=", ">", ">=",
            "+", "-", "*", "/"
    };
    private static final Evaluator[] EVALUATORS = {
            bool((a, b) -> a && b),
            bool((a, b) -> a || b),
            compare((a, b) -> a < b),
            compare((a, b) -> a <= b),
            compare((a, b) -> a > b),
            compare((a, b) -> a >= b),
            arithmetic(Float::sum),
            arithmetic((a, b) -> a - b),
            arithmetic((a, b) -> a * b),
            arithmetic((a, b) -> b == 0 ? 0 : (a / b))
    };

    public static final int AND = 0;
    public static final int OR = 1;
    public static final int LESS_THAN = 2;
    public static final int LESS_THAN_OR_EQUAL = 3;
    public static final int GREATER_THAN = 4;
    public static final int GREATER_THAN_OR_EQUAL = 5;
    public static final int ADD = 6;
    public static final int SUBTRACT = 7;
    public static final int MULTIPLY = 8;
    public static final int DIVIDE = 9;

    private final int code;
    private final Expression left;
    private final Expression right;

    public InfixExpression(
            int code,
            Expression left,
            Expression right
    ) {
        this.code = code;
        this.left = left;
        this.right = right;
    }

    @Override
    public Object eval(EvalContext context) {
        return EVALUATORS[code].eval(context, left, right);
    }

    @Override
    public String toSource() {
        return left.toSource() + " "
                + SYMBOLS[code]
                + " " + right.toSource();
    }

    @Override
    public String toString() {
        return NAMES[code] + "(" + left + ", " + right + ")";
    }

    private interface Evaluator {
        Object eval(EvalContext ctx, Expression a, Expression b);
    }

    private static Evaluator bool(BooleanOperator op) {
        return (ctx, a, b) -> op.operate(a.evalAsBoolean(ctx), b.evalAsBoolean(ctx)) ? 1F : 0F;
    }

    private static Evaluator compare(Comparator comp) {
        return (ctx, a, b) -> comp.compare(a.evalAsFloat(ctx), b.evalAsFloat(ctx)) ? 1F : 0F;
    }

    private static Evaluator arithmetic(ArithmeticOperator op) {
        return (ctx, a, b) -> op.operate(a.evalAsFloat(ctx), b.evalAsFloat(ctx));
    }

    private interface BooleanOperator {
        boolean operate(boolean a, boolean b);
    }

    private interface Comparator {
        boolean compare(float a, float b);
    }

    private interface ArithmeticOperator {
        float operate(float a, float b);
    }

}
