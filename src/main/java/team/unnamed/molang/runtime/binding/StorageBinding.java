package team.unnamed.molang.runtime.binding;

import java.util.HashMap;
import java.util.Map;

public class StorageBinding implements ObjectBinding {

    private final Map<String, Object> values = new HashMap<>();
    private final boolean writeable;

    public StorageBinding(boolean writeable) {
        this.writeable = writeable;
    }

    public StorageBinding() {
        this.writeable = true;
    }

    @Override
    public Object getProperty(String name) {
        return values.get(name);
    }

    @Override
    public void setProperty(String name, Object value) {
        if (writeable) {
            values.put(name, value);
        }
    }

    public void clear() {
        values.clear();
    }

}
