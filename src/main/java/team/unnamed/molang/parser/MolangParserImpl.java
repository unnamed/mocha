package team.unnamed.molang.parser;

import team.unnamed.molang.lexer.MolangLexer;
import team.unnamed.molang.lexer.Token;
import team.unnamed.molang.lexer.TokenKind;
import team.unnamed.molang.parser.ast.Expression;
import team.unnamed.molang.lexer.Tokens;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Standard implementation of {@link MolangParser},
 * it's Hephaestus-MoLang parser since some MoLang
 * characteristics may change
 *
 * @see Tokens
 * @see Expression
 */
final class MolangParserImpl implements MolangParser {

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
            MolangLexer lexer = MolangLexer.lexer(reader);
            Token current = lexer.next(); // initial next call
            while (true) {
                if (current.kind() == TokenKind.EOF) {
                    break;
                }
                expressions.add(parse(lexer));
                current = lexer.current();

                if (current.kind() == TokenKind.EOF) {
                    // end reached, break
                    break;
                } else if (current.kind() != TokenKind.SEMICOLON) {
                    throw new ParseException("Expected a semicolon, but was " + current, lexer.cursor());
                }

                current = lexer.next();
            }
        } catch (ParseException e) {
            throw e;
        } catch (IOException e) {
            throw new ParseException(e, null);
        }

        return expressions;
    }

}
