package team.unnamed.molang.runtime.binding;

public final class ValueConversions {

    private ValueConversions() {
    }

    public static boolean asBoolean(Object obj) {
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        } else if (obj instanceof Number) {
            // '0' is considered false here, anything else
            // is considered true.
            return ((Number) obj).floatValue() != 0;
        } else {
            return true;
        }
    }

    public static float asFloat(Object obj) {
        if (obj instanceof Boolean) {
            return ((Boolean) obj) ? 1 : 0;
        } else if (!(obj instanceof Number)) {
            return 0;
        } else {
            return ((Number) obj).floatValue();
        }
    }

    public static double toDouble(Object obj) {
        if (obj instanceof Boolean) {
            return ((Boolean) obj) ? 1.0D : 0.0D;
        } else if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        } else {
            return 0D;
        }
    }

}
