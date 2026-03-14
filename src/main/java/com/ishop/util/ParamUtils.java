package com.ishop.util;

import java.math.BigDecimal;
import java.util.Map;

public class ParamUtils {

    public static String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }

    public static Long getLong(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).longValue();
        return Long.parseLong(val.toString());
    }

    public static Integer getInt(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).intValue();
        return Integer.parseInt(val.toString());
    }

    public static BigDecimal getDecimal(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return null;
        if (val instanceof BigDecimal) return (BigDecimal) val;
        return new BigDecimal(val.toString());
    }

    public static String getStringDefault(Map<String, Object> map, String key, String defaultVal) {
        String val = getString(map, key);
        return val != null && !val.isEmpty() ? val : defaultVal;
    }

    public static Integer getIntDefault(Map<String, Object> map, String key, int defaultVal) {
        Integer val = getInt(map, key);
        return val != null ? val : defaultVal;
    }
}
