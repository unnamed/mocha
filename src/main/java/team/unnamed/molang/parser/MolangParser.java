package team.unnamed.molang.parser;

import team.unnamed.molang.parser.ast.Expression;
import team.unnamed.molang.lexer.Cursor;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

/**
 * Responsible for parsing MoLang expressions
 * from simple char streams to evaluable
 * {@link Expression} instances
 */
public interface MolangParser {

    /**
     * Parses the data from the given {@code reader}
     * to a {@link List} of {@link Expression}
     *
     * <strong>Note that this method won't close
     * the given {@code reader}</strong>
     *
     * @throws ParseException If read failed or there
     * are syntax errors in the script
     */
    List<Expression> parse(Reader reader) throws ParseException;

    /**
     * Parses the given {@code string} to a list of
     * {@link Expression}
     *
     * @param string The MoLang string
     * @return The list of parsed expressions
     * @throws ParseException If parsing fails
     */
    default List<Expression> parse(String string) throws ParseException {
        try (Reader reader = new StringReader(string)) {
            return parse(reader);
        } catch (ParseException e) {
            throw e;
        } catch (IOException e) {
            throw new ParseException("Failed to close string reader", e, new Cursor(0, 0));
        }
    }

    /**
     * Returns a {@link MolangParser} instance
     *
     * @return The MoLang parser instance
     */
    static MolangParser parser() {
        return new MolangParserImpl();
    }

}
