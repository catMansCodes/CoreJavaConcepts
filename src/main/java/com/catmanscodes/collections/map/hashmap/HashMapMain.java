package com.catmanscodes.collections.map.hashmap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HashMapMain {
    public static void main(String[] args) {

        Map<Integer, String> map = new HashMap<>();

        map.put(7, "Java");
        map.put(5, "JavaScript");
        map.put(6, "Spring");
        map.put(3, "Microservice");
        map.put(1, "MySql");
        map.put(4, "Hibernate");
        map.put(2, "JDBC");

        System.out.println("entire map :" + map);

        System.out.println("3rd key :" + map.get(3)); // microservice

        System.out.println("map size :" + map.size());

        System.out.println("is empty map: " + map.isEmpty());

        Collection<String> values = map.values();
        System.out.println("Values: " + values);

        Set<Integer> keys = map.keySet();
        System.out.println("Keys: " + keys);

        System.out.println("Print map using for loop method 1:");

        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }

        System.out.println("Print map using for loop method 2:");

        for (int mapKey : map.keySet()) {
            System.out.println(mapKey + " : " + map.get(mapKey));
        }

        System.out.println("Print map using for each loop method 3:");

        map.forEach((k, v) -> System.out.println(k + " : " + v));

        System.out.println("Print map using for stream method 4:");

        map.entrySet()
                .stream()
                .forEach(entry -> System.out.println(entry.getKey() + " : " + entry.getValue()));

    }
}
