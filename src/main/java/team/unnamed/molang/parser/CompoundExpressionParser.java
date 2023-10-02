package team.unnamed.molang.parser;

import team.unnamed.molang.parser.ast.Expression;
import team.unnamed.molang.parser.ast.TernaryConditionalExpression;
import team.unnamed.molang.parser.ast.AssignExpression;
import team.unnamed.molang.parser.ast.ConditionalExpression;
import team.unnamed.molang.parser.ast.InfixExpression;
import team.unnamed.molang.parser.ast.NullCoalescingExpression;
import team.unnamed.molang.parser.ast.CallExpression;
import team.unnamed.molang.lexer.MolangLexer;
import team.unnamed.molang.lexer.Token;
import team.unnamed.molang.lexer.TokenKind;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

final class CompoundExpressionParser {

    private CompoundExpressionParser() {
    }

    static Expression parseCompound(MolangLexer lexer, Expression left, int attachmentPower) throws IOException {
        Token current = lexer.current();

        switch (current.kind()) {
            case RPAREN:
            case EOF:
                return left;
            case LPAREN: { // CALL EXPRESSION: "left("
                lexer.next();
                List<Expression> arguments = new ArrayList<>();

                // start reading the arguments
                while (true) {
                    arguments.add(MolangParserImpl.parseCompoundExpression(lexer));
                    // update current character
                    current = lexer.current();
                    if (current.kind() == TokenKind.EOF) {
                        throw new ParseException("Found EOF before closing RPAREN", null);
                    } else if (current.kind() == TokenKind.RPAREN) {
                        lexer.next();
                        break;
                    } else {
                        if (current.kind() != TokenKind.COMMA) {
                            throw new ParseException("Expected a comma", null);
                        }
                        lexer.next();
                    }
                }

                return new CallExpression(left, arguments);
            }
            case QUES: {
                lexer.next();
                Expression trueValue = MolangParserImpl.parseCompoundExpression(lexer);

                if (lexer.current().kind() == TokenKind.COLON) {
                    // then it's a ternary expression, since there is a ':', indicating the next expression
                    lexer.next();
                    return new TernaryConditionalExpression(left, trueValue, MolangParserImpl.parseCompoundExpression(lexer));
                } else {
                    return new ConditionalExpression(left, trueValue);
                }
            }
            case QUESQUES: {
                if (attachmentPower > OperatorPrecedence.NULL_COALESCING) break;
                lexer.next();
                return new NullCoalescingExpression(left, MolangParserImpl.parseCompoundExpression(lexer, OperatorPrecedence.NULL_COALESCING));
            }
            case EQ: {
                lexer.next();
                return new AssignExpression(left, MolangParserImpl.parseCompoundExpression(lexer));
            }
        }

        // check for infix expressions
        int code;
        int precedence;

        switch (current.kind()) {
            case AMPAMP: {
                code = InfixExpression.AND;
                precedence = OperatorPrecedence.AND;
                break;
            }
            case BARBAR: {
                code = InfixExpression.OR;
                precedence = OperatorPrecedence.OR;
                break;
            }
            case LT: {
                code = InfixExpression.LESS_THAN;
                precedence = OperatorPrecedence.COMPARISON;
                break;
            }
            case LTE: {
                code = InfixExpression.LESS_THAN_OR_EQUAL;
                precedence = OperatorPrecedence.COMPARISON;
                break;
            }
            case GT: {
                code = InfixExpression.GREATER_THAN;
                precedence = OperatorPrecedence.COMPARISON;
                break;
            }
            case GTE: {
                code = InfixExpression.GREATER_THAN_OR_EQUAL;
                precedence = OperatorPrecedence.COMPARISON;
                break;
            }
            case PLUS: {
                code = InfixExpression.ADD;
                precedence = OperatorPrecedence.ADDITION_AND_SUBTRACTION;
                break;
            }
            case SUB: {
                code = InfixExpression.SUBTRACT;
                precedence = OperatorPrecedence.ADDITION_AND_SUBTRACTION;
                break;
            }
            case STAR: {
                code = InfixExpression.MULTIPLY;
                precedence = OperatorPrecedence.MULTIPLICATION_AND_DIVISION;
                break;
            }
            case SLASH: {
                code = InfixExpression.DIVIDE;
                precedence = OperatorPrecedence.MULTIPLICATION_AND_DIVISION;
                break;
            }
            default: {
                return left;
            }
        }

        if (attachmentPower > precedence) {
            return left;
        }

        lexer.next();
        return new InfixExpression(code, left, MolangParserImpl.parseCompoundExpression(lexer, precedence));

    }

}
