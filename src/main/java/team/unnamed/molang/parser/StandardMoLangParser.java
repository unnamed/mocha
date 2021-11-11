package team.unnamed.molang.parser;

import team.unnamed.molang.ast.*;
import team.unnamed.molang.ast.binary.AccessExpression;
import team.unnamed.molang.ast.binary.ConditionalExpression;
import team.unnamed.molang.ast.binary.InfixExpression;
import team.unnamed.molang.ast.binary.AssignExpression;
import team.unnamed.molang.ast.binary.NullCoalescingExpression;
import team.unnamed.molang.ast.composite.CallExpression;
import team.unnamed.molang.ast.composite.ExecutionScopeExpression;
import team.unnamed.molang.ast.simple.DoubleExpression;
import team.unnamed.molang.ast.simple.IdentifierExpression;
import team.unnamed.molang.ast.simple.StringExpression;
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
public class StandardMoLangParser
        implements MoLangParser {

    private void failUnexpectedToken(ParseContext context, char current, char expected)
            throws ParseException {
        throw new ParseException(
                "Unexpected token: '" + current + "'. Expected: '" + expected + '\'',
                context.getCursor()
        );
    }

    private void assertToken(ParseContext context, char expected) throws ParseException {
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
    private String readWord(ParseContext context) throws ParseException {
        StringBuilder builder = new StringBuilder();
        int current = context.getCurrent();
        do {
            builder.append((char) current);
        } while (Tokens.isValidIdentifierContinuation(current = context.next()));
        // skip whitespace
        context.skipWhitespace();
        return builder.toString();
    }

    private Expression parseSingle(ParseContext context) throws ParseException {
        int current = context.getCurrent();

        //#region Expression inside parenthesis
        if (current == '(') {
            context.nextNoWhitespace();
            // wrapped expression: (expression)
            Expression expression = parse(context);
            assertToken(context, ')');
            // skip the closing parenthesis and
            // following spaces
            context.nextNoWhitespace();
            return new WrappedExpression(expression);
        }
        //#endregion

        //#region Execution scope
        if (current == '{') {
            context.nextNoWhitespace();

            List<Expression> expressions = new ArrayList<>();
            while (true) {
                expressions.add(parse(context));
                current = context.getCurrent();
                if (current == '}') {
                    // skip last '}' and next whitespace
                    context.nextNoWhitespace();
                    break;
                } else if (current == -1) {
                    // end reached but not closed yet huh?
                    throw new ParseException(
                            "Found the end before the execution scope closing token",
                            context.getCursor()
                    );
                } else {
                    assertToken(context, ';');
                    // skip current semicolon and
                    // following whitespace
                    context.nextNoWhitespace();
                }
            }

            return new ExecutionScopeExpression(expressions);
        }
        //#endregion

        //#region Identifier expression and keywords
        if (Tokens.isValidForIdentifier(current)) {
            String identifier = readWord(context);

            switch (identifier) {
                case "true":
                    return new DoubleExpression(1D);
                case "false":
                    return new DoubleExpression(0F);
                case "return":
                    return new ReturnExpression(parse(context));
                default:
                    return new IdentifierExpression(identifier);
            }
        }
        //#endregion

        //#region String literal expression
        if (current == Tokens.QUOTE) {
            StringBuilder builder = new StringBuilder();
            while ((current = context.next()) != Tokens.QUOTE && current != -1) {
                builder.append((char) current);
            }

            // it must be closed with 'QUOTE'
            if (current == -1) {
                throw new ParseException(
                        "Found the end before the closing quote",
                        context.getCursor()
                );
            }

            // skip the last quote and following whitespaces
            context.nextNoWhitespace();
            return new StringExpression(builder.toString());
        }
        //#endregion

        //#region Float literal expression
        if (Character.isDigit(current)) {
            return DoubleExpression.parse(context, 1);
        }
        //#endregion

        //#region Negation
        if (current == Tokens.HYPHEN) {
            current = context.nextNoWhitespace();
            if (Character.isDigit(current)) {
                // if negated expression is numeral, make it
                // negative instead of creating a negation expression
                return DoubleExpression.parse(context, -1);
            } else {
                Expression expression = parseSingle(context);
                return new NegationExpression(expression, Tokens.HYPHEN);
            }
        } else if (current == Tokens.EXCLAMATION) {
            context.nextNoWhitespace();
            return new NegationExpression(parseSingle(context), Tokens.EXCLAMATION);
        }
        //#endregion

        return new DoubleExpression(0F);
    }

    private Expression parseMultiplication(ParseContext context, Expression left)
        throws ParseException {
        int current = context.getCurrent();
        if (current == '*') {
            context.nextNoWhitespace();
            Expression right = parseSingle(context);
            return new InfixExpression(InfixExpression.MULTIPLY, left, right);
        } else if (current == '/') {
            context.nextNoWhitespace();
            Expression right = parseSingle(context);
            return new InfixExpression(InfixExpression.DIVIDE, left, right);
        }
        return left;
    }

    private Expression parseAddition(ParseContext context, Expression left)
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

    private Expression parse(ParseContext context, Expression left) throws ParseException {
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

        //#region Dot access expression
        if (current == Tokens.DOT) {
            if (!Tokens.isValidForIdentifier(context.nextNoWhitespace())) {
                throw new ParseException(
                        "Unexpected token; expected a valid field token",
                        context.getCursor()
                );
            }
            return new AccessExpression(left, readWord(context).toLowerCase());
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

    private Expression parse(ParseContext context) throws ParseException {
        Expression expression = parseSingle(context);
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
