package team.unnamed.molang.parser;

import team.unnamed.molang.lexer.Cursor;
import team.unnamed.molang.lexer.MolangLexer;
import team.unnamed.molang.parser.ast.Expression;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;

/**
 * Parser for the Molang language.
 *
 * <p>The parser converts token streams to expression
 * streams</p>
 *
 * <p>This is the next level after lexical analysis
 * done by {@link team.unnamed.molang.lexer.MolangLexer}</p>
 *
 * @since 1.0.0
 */
public /* sealed */ interface MolangParser /* permits MolangParserImpl */ extends Closeable {

    MolangLexer lexer();

    default Cursor cursor() {
        return lexer().cursor();
    }

    Expression current();

    Expression next() throws IOException;

    @Override
    void close() throws IOException;

    /**
     * Returns a {@link MolangParser} instance
     *
     * @return The MoLang parser instance
     */
    static MolangParser parser(MolangLexer lexer) throws IOException {
        return new MolangParserImpl(lexer);
    }

    static MolangParser parser(Reader reader) throws IOException {
        return parser(MolangLexer.lexer(reader));
    }

}
