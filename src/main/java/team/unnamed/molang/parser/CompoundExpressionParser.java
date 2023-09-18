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

    static Expression parseCompound(MolangLexer lexer, Expression left) throws IOException {
        Token current = lexer.current();

        //#region Function call expression
        switch (current.kind()) {
            case LPAREN: {
                lexer.next();
                List<Expression> arguments = new ArrayList<>();

                // start reading the arguments
                while (true) {
                    arguments.add(MolangParserImpl.parse(lexer));
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
                return new InfixExpression(InfixExpression.AND, left, MolangParserImpl.parse(lexer));
            }
            case BARBAR: {
                lexer.next();
                return new InfixExpression(InfixExpression.OR, left, MolangParserImpl.parse(lexer));
            }
            case LT: {
                lexer.next();
                return new InfixExpression(InfixExpression.LESS_THAN, left, MolangParserImpl.parse(lexer));
            }
            case LTE: {
                lexer.next();
                return new InfixExpression(InfixExpression.LESS_THAN_OR_EQUAL, left, MolangParserImpl.parse(lexer));
            }
            case GT: {
                lexer.next();
                return new InfixExpression(InfixExpression.GREATER_THAN, left, MolangParserImpl.parse(lexer));
            }
            case GTE: {
                lexer.next();
                return new InfixExpression(InfixExpression.GREATER_THAN_OR_EQUAL, left, MolangParserImpl.parse(lexer));
            }
            case QUESQUES: {
                lexer.next();
                return new NullCoalescingExpression(left, MolangParserImpl.parse(lexer));
            }
            case QUES: {
                lexer.next();
                Expression trueValue = MolangParserImpl.parse(lexer);

                if (lexer.current().kind() == TokenKind.COLON) {
                    // then it's a ternary expression, since there is a ':', indicating the next expression
                    lexer.next();
                    return new TernaryConditionalExpression(left, trueValue, MolangParserImpl.parse(lexer));
                } else {
                    return new ConditionalExpression(left, trueValue);
                }
            }
            case EQ: {
                lexer.next();
                return new AssignExpression(left, MolangParserImpl.parse(lexer));
            }
            case PLUS: {
                lexer.next();
                return new InfixExpression(InfixExpression.ADD, left, MolangParserImpl.parse(lexer));
            }
            case SUB: {
                lexer.next();
                return new InfixExpression(InfixExpression.SUBTRACT, left, MolangParserImpl.parse(lexer));
            }
            case STAR: {
                lexer.next();
                return new InfixExpression(InfixExpression.MULTIPLY, left, SingleExpressionParser.parseSingle(lexer));
            }
            case SLASH: {
                lexer.next();
                return new InfixExpression(InfixExpression.DIVIDE, left, SingleExpressionParser.parseSingle(lexer));
            }
        }

        return left;
    }

}
