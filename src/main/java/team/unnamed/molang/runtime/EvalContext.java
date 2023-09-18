package team.unnamed.molang.runtime;

import javax.script.Bindings;

public class EvalContext {

    private final Bindings bindings;
    private Object returnValue;

    public EvalContext(Bindings bindings) {
        this.bindings = bindings;
    }

    public Object getBinding(String name) {
        return bindings.get(name);
    }

    /**
     * Sets the scope return value, commonly set
     * by a return expression
     *
     * @param returnValue The scope return value,
     *                    may be null
     */
    public void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }

    /**
     * Returns and deletes the current return value
     * set by previous evaluations, result may be
     * null.
     */
    public Object popReturnValue() {
        Object value = this.returnValue;
        this.returnValue = null;
        return value;
    }

}
