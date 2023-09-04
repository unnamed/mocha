package team.unnamed.molang;

import team.unnamed.molang.ast.Expression;
import team.unnamed.molang.binding.StorageBinding;
import team.unnamed.molang.context.EvalContext;
import team.unnamed.molang.parser.MoLangParser;
import team.unnamed.molang.parser.StandardMoLangParser;

import javax.script.Bindings;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;

final class MoLangEngineImpl implements MoLangEngine {

    private final MoLangParser parser = new StandardMoLangParser();

    private final Map<String, Object> bindings;

    MoLangEngineImpl(MoLangEngine.Builder builder) {
        this.bindings = builder.bindings;
    }

    public Object eval(Reader reader) throws ScriptException {
        try {
            Bindings bindings = new SimpleBindings();
            bindings.putAll(this.bindings);

            // temporal storage
            StorageBinding temp = new StorageBinding();
            bindings.put("temp", temp);

            // temporal
            List<Expression> expressions = parser.parse(reader);

            EvalContext context = new EvalContext(bindings);
            Object lastResult = 0;

            for (Expression expression : expressions) {
                lastResult = expression.eval(context);
                Object returnValue = context.popReturnValue();
                if (returnValue != null) {
                    lastResult = returnValue;
                    break;
                }
            }
            temp.clear();

            return lastResult;
        } catch (IOException e) {
            throw new ScriptException(e);
        }
    }

}
