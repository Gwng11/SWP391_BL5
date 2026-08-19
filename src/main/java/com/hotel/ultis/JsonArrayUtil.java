package com.hotel.ultis;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Minimal JSON string-array codec for the existing amenities_json/images_json schema. */
public final class JsonArrayUtil {
    private JsonArrayUtil() {}

    public static List<String> parse(String json) {
        List<String> values = new ArrayList<>();
        if (json == null || json.isBlank()) return values;
        String source = json.trim();
        if (source.length() < 2 || source.charAt(0) != '[' || source.charAt(source.length() - 1) != ']')
            throw new IllegalArgumentException("Dữ liệu JSON không hợp lệ");

        int i = 1;
        while (i < source.length() - 1) {
            while (i < source.length() - 1 && (Character.isWhitespace(source.charAt(i)) || source.charAt(i) == ',')) i++;
            if (i >= source.length() - 1) break;
            if (source.charAt(i) != '"') throw new IllegalArgumentException("JSON chỉ được chứa chuỗi");
            i++;
            StringBuilder value = new StringBuilder();
            boolean closed = false;
            while (i < source.length() - 1) {
                char ch = source.charAt(i++);
                if (ch == '"') { closed = true; break; }
                if (ch == '\\') {
                    if (i >= source.length() - 1) throw new IllegalArgumentException("JSON escape không hợp lệ");
                    char escaped = source.charAt(i++);
                    switch (escaped) {
                        case '"', '\\', '/' -> value.append(escaped);
                        case 'b' -> value.append('\b');
                        case 'f' -> value.append('\f');
                        case 'n' -> value.append('\n');
                        case 'r' -> value.append('\r');
                        case 't' -> value.append('\t');
                        default -> throw new IllegalArgumentException("JSON escape không được hỗ trợ");
                    }
                } else value.append(ch);
            }
            if (!closed) throw new IllegalArgumentException("JSON string chưa đóng");
            values.add(value.toString());
            while (i < source.length() - 1 && Character.isWhitespace(source.charAt(i))) i++;
            if (i < source.length() - 1 && source.charAt(i) != ',')
                throw new IllegalArgumentException("JSON array không hợp lệ");
        }
        return values;
    }

    public static String toJson(List<String> values) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) json.append(',');
            json.append('"').append(escape(values.get(i))).append('"');
        }
        return json.append(']').toString();
    }

    public static String addUnique(String json, String rawValue) {
        String value = requireValue(rawValue);
        Set<String> values = new LinkedHashSet<>(parse(json));
        if (!values.add(value)) throw new IllegalArgumentException("Giá trị đã tồn tại");
        return toJson(new ArrayList<>(values));
    }

    public static String replace(String json, int index, String rawValue) {
        List<String> values = parse(json);
        checkIndex(values, index);
        String value = requireValue(rawValue);
        if (values.stream().anyMatch(v -> v.equalsIgnoreCase(value))
                && !values.get(index).equalsIgnoreCase(value))
            throw new IllegalArgumentException("Giá trị đã tồn tại");
        values.set(index, value);
        return toJson(values);
    }

    public static String remove(String json, int index) {
        List<String> values = parse(json);
        checkIndex(values, index);
        values.remove(index);
        return toJson(values);
    }

    public static String move(String json, int index, int direction) {
        List<String> values = parse(json);
        checkIndex(values, index);
        int target = index + direction;
        checkIndex(values, target);
        String value = values.remove(index);
        values.add(target, value);
        return toJson(values);
    }

    private static String requireValue(String rawValue) {
        if (ValidationUtil.isBlank(rawValue)) throw new IllegalArgumentException("Giá trị không được để trống");
        String value = rawValue.trim();
        if (value.length() > 500) throw new IllegalArgumentException("Giá trị quá dài");
        return value;
    }

    private static void checkIndex(List<String> values, int index) {
        if (index < 0 || index >= values.size()) throw new IllegalArgumentException("Vị trí không hợp lệ");
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
