package team.unnamed.molang.lexer;


import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;

/**
 * Lexical analyzer for the Molang language.
 *
 * <p>The lexical analyzer converts character streams
 * to token streams</p>
 *
 * @since 1.0.0
 */
public /*sealed*/ interface MolangLexer /* permits MolangLexerImpl */ extends Closeable {

    Cursor cursor();

    Token current();

    Token next() throws IOException;

    @Override
    void close() throws IOException;

    static MolangLexer lexer(Reader reader) throws IOException {
        return new MolangLexerImpl(reader);
    }

}
