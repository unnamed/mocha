package team.unnamed.molang.runtime.binding;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Represents an object-like binding,
 * these objects can have properties
 * (or fields) that can be read and
 * sometimes written
 */
public class ObjectBinding {

    private final Map<String, Object> properties;
    private boolean blocked = false;

    protected ObjectBinding(Map<String, Object> propertiesMap) {
        this.properties = propertiesMap;
    }

    public ObjectBinding() {
        this(new HashMap<>());
    }

    /**
     * Gets the property value in this
     * object with the given {@code name}
     */
    public Object getProperty(String name) {
        return properties.get(name);
    }

    /**
     * Sets the property with the given
     * {@code name} to the specified {@code value},
     * may not be supported
     */
    public void setProperty(String name, Object value) {
        if (blocked) {
            throw new IllegalStateException("This object binding has been blocked!");
        }
        properties.put(name, value);
    }

    public void setAllFrom(ObjectBinding binding) {
        requireNonNull(binding, "binding");
        if (blocked) {
            throw new IllegalStateException("This object binding has been blocked!");
        }
        this.properties.putAll(binding.properties);
    }

    public boolean blocked() {
        return blocked;
    }

    public void block() {
        if (blocked) {
            throw new IllegalStateException("Already blocked!");
        }
        blocked = true;
    }

}
