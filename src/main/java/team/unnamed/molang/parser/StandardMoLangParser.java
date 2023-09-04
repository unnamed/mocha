package team.unnamed.molang.parser;

import team.unnamed.molang.ast.*;
import team.unnamed.molang.ast.binary.AccessExpression;
import team.unnamed.molang.ast.binary.ConditionalExpression;
import team.unnamed.molang.ast.binary.InfixExpression;
import team.unnamed.molang.ast.binary.AssignExpression;
import team.unnamed.molang.ast.binary.NullCoalescingExpression;
import team.unnamed.molang.ast.composite.CallExpression;
import team.unnamed.molang.context.ParseContext;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Standard implementation of {@link MoLangParser},
 * it's Hephaestus-MoLang parser since some MoLang
 * characteristics may change
 *
 * <p>There are some contracts for the parse methods:
 *
 * - After an invoke, the {@link ParseContext#getCurrent()} should
 *   return a non-whitespace new token that the next parse method can parse
 *
 * - They must assume that the {@link ParseContext#getCurrent()} will be
 *   a new non-whitespace token when they are called
 * </p>
 *
 * @see Tokens
 * @see Expression
 */
public class StandardMoLangParser implements MoLangParser {

    static void failUnexpectedToken(ParseContext context, char current, char expected)
            throws ParseException {
        throw new ParseException(
                "Unexpected token: '" + current + "'. Expected: '" + expected + '\'',
                context.getCursor()
        );
    }

    static void assertToken(ParseContext context, char expected) throws ParseException {
        int current;
        if ((current = context.getCurrent()) != expected) {
            // must be closed
            failUnexpectedToken(context, (char) current, expected);
        }
    }

    /**
     * Reads a 'word' or 'identifier' without any other
     * checks, assumes that current token is valid for
     * a word
     */
    static String readWord(ParseContext context) throws ParseException {
        StringBuilder builder = new StringBuilder();
        int current = context.getCurrent();
        do {
            builder.append((char) current);
        } while (Tokens.isValidIdentifierContinuation(current = context.next()));
        // skip whitespace
        context.skipWhitespace();
        return builder.toString();
    }

    static Expression parseMultiplication(ParseContext context, Expression left)
        throws ParseException {
        int current = context.getCurrent();
        if (current == '*') {
            context.nextNoWhitespace();
            Expression right = SingleExpressionParser.parseSingle(context);
            return new InfixExpression(InfixExpression.MULTIPLY, left, right);
        } else if (current == '/') {
            context.nextNoWhitespace();
            Expression right = SingleExpressionParser.parseSingle(context);
            return new InfixExpression(InfixExpression.DIVIDE, left, right);
        }
        return left;
    }

    static Expression parseAddition(ParseContext context, Expression left)
        throws ParseException {
        int current = context.getCurrent();
        if (current == '+') {
            context.nextNoWhitespace();
            Expression right = parse(context);
            return new InfixExpression(InfixExpression.ADD, left, right);
        } else if (current == '-') {
            context.nextNoWhitespace();
            Expression right = parse(context);
            return new InfixExpression(InfixExpression.SUBTRACT, left, right);
        }
        // try fallback-ing to multiplication/division
        return parseMultiplication(context, left);
    }

    static Expression parse(ParseContext context, Expression left) throws ParseException {
        int current = context.getCurrent();

        //#region Function call expression
        if (current == '(') {

            List<Expression> arguments = new ArrayList<>();

            // skip the initial parenthesis and
            // following spaces
            context.nextNoWhitespace();

            // start reading the arguments
            while (true) {
                arguments.add(parse(context));
                // update current character
                current = context.getCurrent();
                if (current == -1) {
                    failUnexpectedToken(context, (char) -1, ')');
                } else if (current == ')') {
                    // skip closing parenthesis and
                    // following whitespace
                    context.nextNoWhitespace();
                    break;
                } else {
                    assertToken(context, ',');
                    // skip current comma and following whitespace
                    context.nextNoWhitespace();
                }
            }

            return new CallExpression(left, arguments);
        }
        //#endregion

        //#region Logical Operators
        if (current == Tokens.AMPERSAND) {
            current = context.next();

            if (current != Tokens.AMPERSAND) {
                failUnexpectedToken(context, (char) current, Tokens.AMPERSAND);
                return null; // should never happen
            }

            // skip second ampersand and next spaces
            context.nextNoWhitespace();
            return new InfixExpression(InfixExpression.AND, left, parse(context));
        } else if (current == Tokens.LINE) {
            current = context.next();

            if (current != Tokens.LINE) {
                failUnexpectedToken(context, (char) current, Tokens.LINE);
                return null; // should never happen
            }

            // skip second line and next spaces
            context.nextNoWhitespace();
            return new InfixExpression(InfixExpression.OR, left, parse(context));
        } else if (current == '<') {
            if (context.next() == '=') {
                context.nextNoWhitespace();
                return new InfixExpression(InfixExpression.LESS_THAN_OR_EQUAL, left, parse(context));
            }

            context.skipWhitespace();
            return new InfixExpression(InfixExpression.LESS_THAN, left, parse(context));
        } else if (current == '>') {
            if (context.next() == '=') {
                context.nextNoWhitespace();
                return new InfixExpression(InfixExpression.GREATER_THAN_OR_EQUAL, left, parse(context));
            }

            context.skipWhitespace();
            return new InfixExpression(InfixExpression.GREATER_THAN, left, parse(context));
        }
        //#endregion

        //#region Null Coalescing, Binary Conditional and Ternary Conditional expressions
        if (current == '?') {
            current = context.next();
            if (current == '?') {
                // then it's null-coalescing expression
                // since there are two '?' together (??)
                context.nextNoWhitespace();
                return new NullCoalescingExpression(left, parse(context));
            } else {
                // then it's a ternary or binary expression, since
                // there is only one '?' token
                context.skipWhitespace();
                Expression trueValue = parse(context);

                if (context.getCurrent() == ':') {
                    // then it's a ternary expression, since there is
                    // a ':', indicating the next expression
                    context.nextNoWhitespace();
                    return new TernaryConditionalExpression(left, trueValue, parse(context));
                } else {
                    return new ConditionalExpression(left, trueValue);
                }
            }
        }
        //#endregion

        //#region Assignation Operators
        if (current == Tokens.EQUAL) {
            context.nextNoWhitespace();
            return new AssignExpression(left, parse(context));
        }
        //#endregion

        return parseAddition(context, left);
    }

    static Expression parse(ParseContext context) throws ParseException {
        Expression expression = SingleExpressionParser.parseSingle(context);
        while (true) {
            Expression compositeExpr = parse(context, expression);
            if (compositeExpr == expression) {
                break;
            } else {
                expression = compositeExpr;
            }
        }
        return expression;
    }

    @Override
    public List<Expression> parse(Reader reader) throws ParseException {

        ParseContext context = new ParseContext(reader);
        // initial next() call
        context.nextNoWhitespace();

        List<Expression> expressions = new ArrayList<>();
        int current;
        while (true) {
            expressions.add(parse(context));
            current = context.getCurrent();
            if (current == -1) {
                // end reached, break
                break;
            } else {
                assertToken(context, ';');
                // skip current semicolon and
                // following whitespace
                context.nextNoWhitespace();
            }
        }

        return expressions;
    }

}
