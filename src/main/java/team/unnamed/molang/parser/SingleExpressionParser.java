package team.unnamed.molang.parser;

import team.unnamed.molang.parser.ast.Expression;
import team.unnamed.molang.parser.ast.NegationExpression;
import team.unnamed.molang.parser.ast.ReturnExpression;
import team.unnamed.molang.parser.ast.WrappedExpression;
import team.unnamed.molang.parser.ast.AccessExpression;
import team.unnamed.molang.parser.ast.ExecutionScopeExpression;
import team.unnamed.molang.parser.ast.DoubleExpression;
import team.unnamed.molang.parser.ast.IdentifierExpression;
import team.unnamed.molang.parser.ast.StringExpression;
import team.unnamed.molang.lexer.MolangLexer;
import team.unnamed.molang.lexer.Token;
import team.unnamed.molang.lexer.TokenKind;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

final class SingleExpressionParser {

    private SingleExpressionParser() {
    }

    static Expression parseSingle(MolangLexer lexer) throws IOException {
        Token token = lexer.current();
        switch (token.kind()) {
            case FLOAT: {
                // found a float literal token!
                lexer.next();
                return new DoubleExpression(Double.parseDouble(token.value()));
            }
            case STRING: {
                // found a string literal token!
                lexer.next();
                return new StringExpression(token.value());
            }
            case TRUE: {
                // found a boolean literal token!
                lexer.next();
                return new DoubleExpression(1.0D);
            }
            case FALSE: {
                // found a false literal token!
                lexer.next();
                return new DoubleExpression(0.0D);
            }
            case LPAREN: {
                lexer.next();
                // wrapped expression: (expression)
                Expression expression = MolangParserImpl.parseCompoundExpression(lexer);
                token = lexer.current();
                if (token.kind() != TokenKind.RPAREN) {
                    throw new ParseException("Non closed expression", null);
                }
                lexer.next();
                return new WrappedExpression(expression);
            }
            case LBRACE: {
                lexer.next();
                List<Expression> expressions = new ArrayList<>();
                while (true) {
                    expressions.add(MolangParserImpl.parseCompoundExpression(lexer));
                    token = lexer.current();
                    if (token.kind() == TokenKind.RBRACE) {
                        lexer.next();
                        break;
                    } else if (token.kind() == TokenKind.EOF) {
                        // end reached but not closed yet huh?
                        throw new ParseException(
                                "Found the end before the execution scope closing token",
                                null
                        );
                    } else {
                        if (token.kind() != TokenKind.SEMICOLON) {
                            throw new ParseException("Missing semicolon", null);
                        }
                        lexer.next();
                    }
                }
                return new ExecutionScopeExpression(expressions);
            }
            // todo: should not be parsed as identifiers
            case FOR_EACH:
            case BREAK:
            case CONTINUE:
            case THIS:
            case IDENTIFIER: {
                Expression expr = new IdentifierExpression(token.value());
                token = lexer.next();
                while (token.kind() == TokenKind.DOT) {
                    token = lexer.next();

                    if (token.kind() != TokenKind.IDENTIFIER) {
                        throw new ParseException("Unexpected token, expected a valid field token", null);
                    }

                    expr = new AccessExpression(expr, token.value());
                    token = lexer.next();
                }
                return expr;
            }
            case SUB: {
                lexer.next();
                return new NegationExpression(parseSingle(lexer), '-');
            }
            case BANG: {
                lexer.next();
                return new NegationExpression(parseSingle(lexer), '!');
            }
            case RETURN: {
                lexer.next();
                return new ReturnExpression(MolangParserImpl.parseCompoundExpression(lexer));
            }
        }

        return new DoubleExpression(0F);
    }

}
