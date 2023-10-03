/*
 * This file is part of molang, licensed under the MIT license
 *
 * Copyright (c) 2021-2023 Unnamed Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package team.unnamed.molang.parser;

import team.unnamed.molang.parser.ast.Expression;
import team.unnamed.molang.parser.ast.TernaryConditionalExpression;
import team.unnamed.molang.parser.ast.ConditionalExpression;
import team.unnamed.molang.parser.ast.InfixExpression;
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
        }

        // check for infix expressions
        InfixExpression.Op op;
        int precedence;

        switch (current.kind()) {
            case AMPAMP: {
                op = InfixExpression.Op.AND;
                precedence = OperatorPrecedence.AND;
                break;
            }
            case BARBAR: {
                op = InfixExpression.Op.OR;
                precedence = OperatorPrecedence.OR;
                break;
            }
            case LT: {
                op = InfixExpression.Op.LT;
                precedence = OperatorPrecedence.COMPARISON;
                break;
            }
            case LTE: {
                op = InfixExpression.Op.LTE;
                precedence = OperatorPrecedence.COMPARISON;
                break;
            }
            case GT: {
                op = InfixExpression.Op.GT;
                precedence = OperatorPrecedence.COMPARISON;
                break;
            }
            case GTE: {
                op = InfixExpression.Op.GTE;
                precedence = OperatorPrecedence.COMPARISON;
                break;
            }
            case PLUS: {
                op = InfixExpression.Op.ADD;
                precedence = OperatorPrecedence.ADDITION_AND_SUBTRACTION;
                break;
            }
            case SUB: {
                op = InfixExpression.Op.SUB;
                precedence = OperatorPrecedence.ADDITION_AND_SUBTRACTION;
                break;
            }
            case STAR: {
                op = InfixExpression.Op.MUL;
                precedence = OperatorPrecedence.MULTIPLICATION_AND_DIVISION;
                break;
            }
            case SLASH: {
                op = InfixExpression.Op.DIV;
                precedence = OperatorPrecedence.MULTIPLICATION_AND_DIVISION;
                break;
            }
            case QUESQUES: {
                op = InfixExpression.Op.NULL_COALESCE;
                precedence = OperatorPrecedence.NULL_COALESCING;
                break;
            }
            case EQ: {
                op = InfixExpression.Op.ASSIGN;
                precedence = OperatorPrecedence.ASSIGN;
                break;
            }
            default: {
                return left;
            }
        }

        if (attachmentPower >= precedence) {
            return left;
        }

        lexer.next();
        return new InfixExpression(op, left, MolangParserImpl.parseCompoundExpression(lexer, precedence));
    }

}
