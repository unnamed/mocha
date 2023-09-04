package team.unnamed.molang.parser;

import team.unnamed.molang.ast.Expression;
import team.unnamed.molang.ast.NegationExpression;
import team.unnamed.molang.ast.ReturnExpression;
import team.unnamed.molang.ast.Tokens;
import team.unnamed.molang.ast.WrappedExpression;
import team.unnamed.molang.ast.binary.AccessExpression;
import team.unnamed.molang.ast.composite.ExecutionScopeExpression;
import team.unnamed.molang.ast.simple.DoubleExpression;
import team.unnamed.molang.ast.simple.IdentifierExpression;
import team.unnamed.molang.ast.simple.StringExpression;
import team.unnamed.molang.context.ParseContext;

import java.util.ArrayList;
import java.util.List;

final class SingleExpressionParser {

    private SingleExpressionParser() {
    }

    static Expression parseSingle(ParseContext context) throws ParseException {
        int current = context.getCurrent();

        //#region Expression inside parenthesis
        if (current == '(') {
            context.nextNoWhitespace();
            // wrapped expression: (expression)
            Expression expression = StandardMoLangParser.parse(context);
            StandardMoLangParser.assertToken(context, ')');
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
                expressions.add(StandardMoLangParser.parse(context));
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
                    StandardMoLangParser.assertToken(context, ';');
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
            String identifier = StandardMoLangParser.readWord(context);

            switch (identifier) {
                case "true":
                    return new DoubleExpression(1D);
                case "false":
                    return new DoubleExpression(0F);
                case "return":
                    return new ReturnExpression(StandardMoLangParser.parse(context));
            }

            // update current
            current = context.getCurrent();
            Expression left = new IdentifierExpression(identifier);

            //#region Dot access expression
            while (current == Tokens.DOT) {
                current = context.nextNoWhitespace();

                if (!Tokens.isValidForIdentifier(current)) {
                    throw new ParseException(
                            "Unexpected token; expected a valid field token",
                            context.getCursor()
                    );
                }

                left = new AccessExpression(
                        left,
                        StandardMoLangParser.readWord(context).toLowerCase()
                );
            }
            //#endregion
            return left;
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

}
