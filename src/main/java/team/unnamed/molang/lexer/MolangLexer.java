package team.unnamed.molang.lexer;

import team.unnamed.molang.ast.Tokens;

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
public final class MolangLexer implements Closeable {

    // the source reader
    private final Reader reader;

    // the current index
    private final Cursor cursor = new Cursor();

    // the current character
    private int current;

    // the current token
    private Token token = null;

    public MolangLexer(Reader reader) throws IOException {
        this.reader = reader;
        this.current = reader.read();
    }

    public Token current() {
        if (token == null) {
            throw new IllegalStateException("No current token");
        }
        return token;
    }

    public Token next() throws IOException {
        return token = next0();
    }

    private Token next0() throws IOException {
        int c = current;
        if (c == -1) {
            // EOF reached
            return new Token(TokenKind.EOF, null, cursor.index(), cursor.index() + 1);
        }

        // skip whitespace (including tabs and newlines)
        while (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
            c = read();
        }

        int start = cursor.index();
        if (Tokens.isDigit(c)) {
            StringBuilder builder = new StringBuilder(8);
            builder.appendCodePoint(c);

            // first char is a digit, continue reading number
            while (Tokens.isDigit(c = read())) {
                builder.appendCodePoint(c);
            }

            if (c == '.') {
                builder.append('.');
                while (Tokens.isDigit(c = read())) {
                    builder.appendCodePoint(c);
                }
            }

            return new Token(TokenKind.FLOAT, builder.toString(), start, cursor.index());
        } else if (Tokens.isValidForWord(c)) {
            // may be an identifier or a keyword
            StringBuilder builder = new StringBuilder();
            do {
                builder.appendCodePoint(c);
            } while (Tokens.isValidForWordContinuation(c = read()));
            String word = builder.toString();
            TokenKind kind;
            switch (word.toLowerCase()) {
                case "loop":
                    kind = TokenKind.LOOP;
                    break;
                case "for_each":
                    kind = TokenKind.FOR_EACH;
                    break;
                case "break":
                    kind = TokenKind.BREAK;
                    break;
                case "continue":
                    kind = TokenKind.CONTINUE;
                    break;
                case "this":
                    kind = TokenKind.THIS;
                    break;
                case "return":
                    kind = TokenKind.RETURN;
                    break;
                case "true":
                    kind = TokenKind.TRUE;
                    break;
                case "false":
                    kind = TokenKind.FALSE;
                    break;
                default:
                    kind = TokenKind.IDENTIFIER;
                    break;
            }

            return new Token(
                    kind,
                    // keywords do not have values
                    kind == TokenKind.IDENTIFIER ? word : null,
                    start,
                    cursor.index()
            );
        } else if (c == '\'') { // single quote means string start
            StringBuilder value = new StringBuilder(16);
            while (true) {
                c = read();
                if (c == -1) {
                    // the heck? you didn't close the string
                    return new Token(TokenKind.ERROR, null, start, cursor.index());
                } else if (c == '\'') {
                    // string was closed!
                    break;
                } else {
                    // TODO: should we allow escaping quotes? should we disallow line breaks?
                    // not end of file nor quote, this is inside the string literal
                    value.appendCodePoint(c);
                }
            }
            // Here, "c" should be a quote, so skip it and give it to the next person
            read();
            return new Token(TokenKind.STRING, value.toString(), start, cursor.index());
        } else {
            int next = read();
            TokenKind tokenKind;
            switch (c) {
                case '.': tokenKind = TokenKind.DOT; break;
                case '!': {
                    if (next == '=') {
                        tokenKind = TokenKind.BANGEQ;
                    } else {
                        tokenKind = TokenKind.BANG;
                    }
                    break;
                }
                case '&': {
                    if (next == '&') {
                        read();
                        tokenKind = TokenKind.AMPAMP;
                    } else {
                        tokenKind = TokenKind.ERROR;
                    }
                    break;
                }
                case '|': {
                    if (next == '|') {
                        read();
                        tokenKind = TokenKind.BARBAR;
                    } else {
                        tokenKind = TokenKind.ERROR;
                    }
                    break;
                }
                case '<': {
                    if (next == '=') {
                        read();
                        tokenKind = TokenKind.LTE;
                    } else {
                        tokenKind = TokenKind.LT;
                    }
                    break;
                }
                case '>': {
                    if (next == '=') {
                        tokenKind = TokenKind.GTE;
                        read();
                    } else {
                        tokenKind = TokenKind.GT;
                    }
                    break;
                }
                case '=': {
                    if (next == '=') {
                        tokenKind = TokenKind.EQEQ;
                        read();
                    } else {
                        tokenKind = TokenKind.EQ;
                    }
                    break;
                }
                case '/': tokenKind = TokenKind.SLASH; break;
                case '*': tokenKind = TokenKind.STAR; break;
                case '+': tokenKind = TokenKind.PLUS; break;
                case '-': {
                    if (next == '>') {
                        tokenKind = TokenKind.ARROW;
                        read();
                    } else {
                        tokenKind = TokenKind.SUB;
                    }
                    break;
                }
                case ',': tokenKind = TokenKind.COMMA; break;
                case '?': {
                    if (next == '?') {
                        tokenKind = TokenKind.QUESQUES;
                        read();
                    } else {
                        tokenKind = TokenKind.QUES;
                    }
                    break;
                }
                case '(': tokenKind = TokenKind.LPAREN; break;
                case ')': tokenKind = TokenKind.RPAREN; break;
                case '{': tokenKind = TokenKind.LBRACE; break;
                case '}': tokenKind = TokenKind.RBRACE; break;
                case ':': tokenKind = TokenKind.COLON; break;
                case '[': tokenKind = TokenKind.LBRACKET; break;
                case ']': tokenKind = TokenKind.RBRACKET; break;
                case ';': tokenKind = TokenKind.SEMICOLON; break;
                default: tokenKind = TokenKind.ERROR; break;
            }
            return new Token(tokenKind, null, start, cursor.index());
        }
    }

    private int read() throws IOException {
        int c = reader.read();
        current = c;
        cursor.push(c);
        return c;
    }

    @Override
    public void close() throws IOException {
        this.reader.close();
    }

}
