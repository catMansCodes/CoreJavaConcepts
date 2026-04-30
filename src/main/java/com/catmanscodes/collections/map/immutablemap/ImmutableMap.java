package com.catmanscodes.collections.map.immutablemap;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ImmutableMap {
    public static void main(String[] args) {

        Map<String, Integer> map = new HashMap<>();

        map.put("Java", 26);
        map.put("Spring", 7);
        map.put("AI", 2);

        Map<String, Integer> stringIntegerMap = Collections.unmodifiableMap(map);
        System.out.println(stringIntegerMap);

        System.out.println("Read okkk :" + stringIntegerMap.get("AI"));
        //stringIntegerMap.put("JSP", 11); // EXCEPTION
        System.out.println(stringIntegerMap);

        Map<Integer, String> map2 = Map.of(1, "CAT", 2, "DOG", 3, "LION");
        System.out.println("MAP 2: " + map2);

        Map<Integer, String> integerStringMap = Map.ofEntries(Map.entry(1, "JAVA"), Map.entry(3, "Spring"), Map.entry(22, "JSP"));
        System.out.println(integerStringMap);

    }
}
