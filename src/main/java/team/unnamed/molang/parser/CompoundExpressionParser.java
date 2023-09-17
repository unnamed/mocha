package team.unnamed.molang.parser;

import team.unnamed.molang.ast.Expression;
import team.unnamed.molang.ast.TernaryConditionalExpression;
import team.unnamed.molang.ast.binary.AssignExpression;
import team.unnamed.molang.ast.binary.ConditionalExpression;
import team.unnamed.molang.ast.binary.InfixExpression;
import team.unnamed.molang.ast.binary.NullCoalescingExpression;
import team.unnamed.molang.ast.composite.CallExpression;
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
                    arguments.add(StandardMoLangParser.parse(lexer));
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
                return new InfixExpression(InfixExpression.AND, left, StandardMoLangParser.parse(lexer));
            }
            case BARBAR: {
                lexer.next();
                return new InfixExpression(InfixExpression.OR, left, StandardMoLangParser.parse(lexer));
            }
            case LT: {
                lexer.next();
                return new InfixExpression(InfixExpression.LESS_THAN, left, StandardMoLangParser.parse(lexer));
            }
            case LTE: {
                lexer.next();
                return new InfixExpression(InfixExpression.LESS_THAN_OR_EQUAL, left, StandardMoLangParser.parse(lexer));
            }
            case GT: {
                lexer.next();
                return new InfixExpression(InfixExpression.GREATER_THAN, left, StandardMoLangParser.parse(lexer));
            }
            case GTE: {
                lexer.next();
                return new InfixExpression(InfixExpression.GREATER_THAN_OR_EQUAL, left, StandardMoLangParser.parse(lexer));
            }
            case QUESQUES: {
                lexer.next();
                return new NullCoalescingExpression(left, StandardMoLangParser.parse(lexer));
            }
            case QUES: {
                lexer.next();
                Expression trueValue = StandardMoLangParser.parse(lexer);

                if (lexer.current().kind() == TokenKind.COLON) {
                    // then it's a ternary expression, since there is a ':', indicating the next expression
                    lexer.next();
                    return new TernaryConditionalExpression(left, trueValue, StandardMoLangParser.parse(lexer));
                } else {
                    return new ConditionalExpression(left, trueValue);
                }
            }
            case EQ: {
                lexer.next();
                return new AssignExpression(left, StandardMoLangParser.parse(lexer));
            }
            case PLUS: {
                lexer.next();
                return new InfixExpression(InfixExpression.ADD, left, StandardMoLangParser.parse(lexer));
            }
            case SUB: {
                lexer.next();
                return new InfixExpression(InfixExpression.SUBTRACT, left, StandardMoLangParser.parse(lexer));
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
