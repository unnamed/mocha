package team.unnamed.molang;

import team.unnamed.molang.binding.Bind;
import team.unnamed.molang.binding.StorageBinding;
import team.unnamed.molang.context.EvalContext;
import team.unnamed.molang.ast.Expression;
import team.unnamed.molang.parser.MoLangParser;
import team.unnamed.molang.parser.StandardMoLangParser;

import javax.script.Bindings;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoLangEngine {

    private final MoLangParser parser = new StandardMoLangParser();
    private final StorageBinding variable = new StorageBinding();

    private final Map<String, Object> globalBindings;

    private MoLangEngine(Builder builder) {
        this.globalBindings = builder.bindings;
    }

    public Object eval(String script) throws ScriptException {
        return eval(new StringReader(script));
    }

    public Object eval(Reader reader) throws ScriptException {
        try {
            Bindings bindings = new SimpleBindings();
            bindings.put("variable", variable);

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

    public static Builder builder() {
        return new Builder();
    }

    public static MoLangEngine createDefault() {
        return new Builder()
                .withDefaultBindings()
                .build();
    }

    public static MoLangEngine createEmpty() {
        return new Builder().build();
    }

    public static class Builder {

        private final Map<String, Object> bindings = new HashMap<>();

        public Builder withDefaultBindings() {
            bindings.put("query", Bind.QUERY_BINDING);
            bindings.put("math", Bind.MATH_BINDING);
            bindings.put("variable", new StorageBinding());
            return this;
        }

        public MoLangEngine build() {
            return new MoLangEngine(this);
        }

    }

}
