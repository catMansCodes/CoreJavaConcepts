package com.catmanscodes.collections.map.linkedhashmap;

import java.util.LinkedHashMap;
import java.util.Map;

public class LinkedHashMapMain {
    public static void main(String[] args) {

        // preserve the order - insertion
        // if the access order is true the recently accessed element will be get at last
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>(16,0.75f,true);

        map.put("Java",25);
        map.put("Go",7);
        map.put("Python",26);

        map.get("Java"); // java will move to last in the map
        System.out.println(map);

        for(Map.Entry<String, Integer> entry: map.entrySet()){
            System.out.println(entry.getKey() +" : " + entry.getValue());
        }
    }
}
