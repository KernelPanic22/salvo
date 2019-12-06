package com.codeoftheweb.salvo;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Util {

    public static Map<String, Object> makeMap(String key, Object value) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(key, value);
        return map;
    }

    public static Map<String,Object> concatMap(Map<String,Object> dto1,Map<String,Object> dto2){
    return Stream.concat(dto1.entrySet().stream(),
                dto2.entrySet().stream())
            .collect(
            Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
