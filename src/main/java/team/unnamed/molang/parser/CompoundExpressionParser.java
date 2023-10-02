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

        //#region Function call expression
        switch (current.kind()) {
            case LPAREN: {
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
            case AMPAMP: {
                lexer.next();
                return new InfixExpression(InfixExpression.AND, left, MolangParserImpl.parseCompoundExpression(lexer, 300));
            }
            case BARBAR: {
                if (attachmentPower > 200) break;
                lexer.next();
                Expression right = MolangParserImpl.parseCompoundExpression(lexer, 200);
                return new InfixExpression(InfixExpression.OR, left, right);
            }
            case LT: {
                if (attachmentPower > 700) break;
                lexer.next();
                return new InfixExpression(InfixExpression.LESS_THAN, left, MolangParserImpl.parseCompoundExpression(lexer, 700));
            }
            case LTE: {
                if (attachmentPower > 700) break;
                lexer.next();
                Expression right = MolangParserImpl.parseCompoundExpression(lexer, 700);
                return new InfixExpression(InfixExpression.LESS_THAN_OR_EQUAL, left, right);
            }
            case GT: {
                if (attachmentPower > 700) break;
                lexer.next();
                return new InfixExpression(InfixExpression.GREATER_THAN, left, MolangParserImpl.parseCompoundExpression(lexer, 700));
            }
            case GTE: {
                if (attachmentPower > 700) break;
                lexer.next();
                Expression right = MolangParserImpl.parseCompoundExpression(lexer, 700);
                return new InfixExpression(InfixExpression.GREATER_THAN_OR_EQUAL, left, right);
            }
            case QUESQUES: {
                lexer.next();
                return new NullCoalescingExpression(left, MolangParserImpl.parseCompoundExpression(lexer));
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
            case EQ: {
                lexer.next();
                return new AssignExpression(left, MolangParserImpl.parseCompoundExpression(lexer, 500));
            }
            case PLUS: {
                if (attachmentPower > 900) break;
                lexer.next();
                return new InfixExpression(InfixExpression.ADD, left, MolangParserImpl.parseCompoundExpression(lexer, 900));
            }
            case SUB: {
                if (attachmentPower > 900) break;
                lexer.next();
                return new InfixExpression(InfixExpression.SUBTRACT, left, MolangParserImpl.parseCompoundExpression(lexer, 900));
            }
            case STAR: {
                if (attachmentPower > 1000) break;
                lexer.next();
                return new InfixExpression(InfixExpression.MULTIPLY, left, MolangParserImpl.parseCompoundExpression(lexer, 1000));
            }
            case SLASH: {
                if (attachmentPower > 1000) break;
                lexer.next();
                return new InfixExpression(InfixExpression.DIVIDE, left, MolangParserImpl.parseCompoundExpression(lexer, 1000));
            }
        }

        return left;
    }

}
