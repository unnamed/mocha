package team.unnamed.molang.parser.ast.simple;

import team.unnamed.molang.parser.ast.Expression;
import team.unnamed.molang.runtime.EvalContext;

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
    public double getValue() {
        return value;
    }

    @Override
    public Object eval(EvalContext context) {
        return value;
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

//    /**
//     * Parses a double value from the given {@code context}
//     * and wraps it into a {@link DoubleExpression} expression
//     *
//     * @param context The context to read from
//     * @param divideByInitial The initial divisor, the value
//     *                        is read as an integer and then
//     *                        divided by this value plus the
//     *                        found digit count after the
//     *                        floating point
//     * @return The parsed double expression, never null
//     * @throws ParseException If stream returns multiple floating
//     * points
//     */
//    public static DoubleExpression parse(
//            float divideByInitial
//    ) throws ParseException {
//
//        int current = context.getCurrent();
//        boolean readingDecimalPart = false;
//        float value = 0;
//        float divideBy = divideByInitial;
//
//        while (true) {
//            if (Character.isDigit(current)) {
//                value *= 10;
//                value += Character.getNumericValue(current);
//                if (readingDecimalPart) {
//                    divideBy *= 10;
//                }
//                current = context.next();
//            } else if (current == Tokens.DOT) {
//                if (readingDecimalPart) {
//                    throw new ParseException(
//                            "Numbers can't have multiple floating points!",
//                            context.getCursor()
//                    );
//                }
//                readingDecimalPart = true;
//                current = context.next();
//            } else {
//                break;
//            }
//        }
//
//        return new DoubleExpression(value / divideBy);
//    }

}
