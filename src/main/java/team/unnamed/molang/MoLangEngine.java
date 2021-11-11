package team.unnamed.molang;

import team.unnamed.molang.binding.Bind;
import team.unnamed.molang.context.EvalContext;
import team.unnamed.molang.ast.Expression;
import team.unnamed.molang.parser.MoLangParser;
import team.unnamed.molang.parser.StandardMoLangParser;

import javax.script.Bindings;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

public class MoLangEngine {

    private final MoLangParser parser = new StandardMoLangParser();

    public Object eval(String script) throws ScriptException {
        return eval(new StringReader(script));
    }

    public Object eval(Reader reader) throws ScriptException {
        try {
            Bindings bindings = new SimpleBindings();
            bindings.put("query", Bind.QUERY_BINDING);
            bindings.put("math", Bind.MATH_BINDING);

            // temporal
            List<Expression> expressions = parser.parse(reader);

            EvalContext evalContext = new EvalContext(bindings);
            Object lastResult = 0;

            for (Expression expression : expressions) {
                System.out.println(expression);
                lastResult = expression.eval(evalContext);
            }

            return lastResult;
        } catch (IOException e) {
            throw new ScriptException(e);
        }
    }

}
