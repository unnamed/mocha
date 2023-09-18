package team.unnamed.molang.parser;

import team.unnamed.molang.ast.*;
import team.unnamed.molang.lexer.MolangLexer;
import team.unnamed.molang.lexer.Token;
import team.unnamed.molang.lexer.TokenKind;
import team.unnamed.molang.parser.ast.Expression;
import team.unnamed.molang.parser.ast.Tokens;

import java.io.IOException;
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
 * - After an invoke, the {@link #getCurrent()} should
 *   return a non-whitespace new token that the next parse method can parse
 *
 * - They must assume that the {@link #getCurrent()} will be
 *   a new non-whitespace token when they are called
 * </p>
 *
 * @see Tokens
 * @see Expression
 */
final class StandardMoLangParser implements MoLangParser {

    static Expression parse(MolangLexer lexer) throws IOException {
        Expression leftHandExpression = SingleExpressionParser.parseSingle(lexer);
        while (true) {
            Expression compositeExpr = CompoundExpressionParser.parseCompound(lexer, leftHandExpression);
            if (compositeExpr == leftHandExpression) {
                break;
            } else {
                leftHandExpression = compositeExpr;
            }
        }
        return leftHandExpression;
    }

    @Override
    public List<Expression> parse(Reader reader) throws ParseException {

        List<Expression> expressions = new ArrayList<>();

        try {
            MolangLexer lexer = new MolangLexer(reader);
            lexer.next(); // initial next call
            Token current;
            while (true) {
                expressions.add(parse(lexer));
                current = lexer.current();
                if (current.kind() == TokenKind.EOF) {
                    // end reached, break
                    break;
                } else {
                    if (current.kind() != TokenKind.SEMICOLON) {
                        throw new ParseException("Expected a semicolon, but was " + current, null);
                    }
                    current = lexer.next();
                }
            }
        } catch (IOException e) {
            throw new ParseException(e, null);
        }

        return expressions;
    }

}
