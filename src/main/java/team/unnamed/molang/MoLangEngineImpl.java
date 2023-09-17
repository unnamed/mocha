package team.unnamed.molang;

import team.unnamed.molang.ast.Expression;
import team.unnamed.molang.binding.StorageBinding;
import team.unnamed.molang.context.EvalContext;
import team.unnamed.molang.parser.MoLangParser;
import team.unnamed.molang.parser.ParseException;

import javax.script.Bindings;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;

final class MoLangEngineImpl implements MoLangEngine {

    private final MoLangParser parser = MoLangParser.parser();

    private final Map<String, Object> bindings;

    MoLangEngineImpl(MoLangEngine.Builder builder) {
        this.bindings = builder.bindings;
    }

    private Bindings createBindings() {
        Bindings bindings = new SimpleBindings();
        bindings.putAll(this.bindings);

        // temporal storage
        StorageBinding temp = new StorageBinding();
        bindings.put("temp", temp);
        return bindings;
    }

    @Override
    public Object eval(List<Expression> expressions) {
        Bindings bindings = createBindings();
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

        return lastResult;
    }

    @Override
    public Object eval(Reader reader) throws ScriptException {
        try {
            return eval(parser.parse(reader));
        } catch (IOException e) {
            throw new ScriptException(e);
        }
    }

    @Override
    public List<Expression> parse(Reader reader) throws ParseException {
        return parser.parse(reader);
    }

}
