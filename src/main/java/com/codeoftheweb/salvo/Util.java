package com.codeoftheweb.salvo;

import java.util.LinkedHashMap;
import java.util.Map;

public class Util {

    public static Map<String, Object> makeMap(String key, Object value) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(key, value);
        return map;
    }
}
