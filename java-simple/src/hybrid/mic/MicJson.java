package hybrid.mic;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/** Exports the public metadata of the demo's MICs without a JSON library. */
public final class MicJson {
    private MicJson() {
    }

    public static String toJson(ExecMIC model) {
        StringBuilder json = new StringBuilder();
        write(json, model, 0);
        return json.append('\n').toString();
    }

    private static void write(StringBuilder json, Object value, int depth) {
        if (value == null || value instanceof Boolean) {
            json.append(value);
        } else if (value instanceof String) {
            json.append(quote((String) value));
        } else if (value instanceof Enum<?>) {
            json.append(quote(((Enum<?>) value).name()));
        } else if (value instanceof Number) {
            if ((value instanceof Double || value instanceof Float)
                    && !Double.isFinite(((Number) value).doubleValue())) {
                throw new IllegalArgumentException("JSON cannot represent non-finite numbers: " + value);
            }
            json.append(value);
        } else if (value instanceof List<?>) {
            writeList(json, (List<?>) value, depth);
        } else if (value.getClass().isArray()) {
            // Keep short numeric arrays, such as a tensor shape or the demo weights, on one line.
            json.append('[');
            for (int i = 0; i < Array.getLength(value); i++) {
                if (i > 0) json.append(", ");
                write(json, Array.get(value, i), depth);
            }
            json.append(']');
        } else {
            writeObject(json, value, depth);
        }
    }

    private static void writeList(StringBuilder json, List<?> values, int depth) {
        json.append('[');
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) json.append(',');
            newline(json, depth + 1);
            write(json, values.get(i), depth + 1);
        }
        if (!values.isEmpty()) newline(json, depth);
        json.append(']');
    }

    private static void writeObject(StringBuilder json, Object value, int depth) {
        json.append('{');
        newline(json, depth + 1);
        json.append("\"@type\": ").append(quote(value.getClass().getSimpleName()));

        // getFields() includes inherited public metadata but not private runtime state.
        Field[] fields = value.getClass().getFields();
        Arrays.sort(fields, Comparator.comparing(Field::getName));
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            Object fieldValue;
            try {
                fieldValue = field.get(value);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Cannot export MIC field " + field, e);
            }
            json.append(',');
            newline(json, depth + 1);
            json.append(quote(field.getName())).append(": ");
            write(json, fieldValue, depth + 1);
        }
        newline(json, depth);
        json.append('}');
    }

    private static void newline(StringBuilder json, int depth) {
        json.append('\n');
        for (int i = 0; i < depth; i++) json.append("  ");
    }

    private static String quote(String text) {
        StringBuilder json = new StringBuilder("\"");
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '"' || ch == '\\') {
                json.append('\\').append(ch);
            } else if (ch < 0x20 || Character.isSurrogate(ch)) {
                json.append(String.format("\\u%04x", (int) ch));
            } else {
                json.append(ch);
            }
        }
        return json.append('"').toString();
    }
}
