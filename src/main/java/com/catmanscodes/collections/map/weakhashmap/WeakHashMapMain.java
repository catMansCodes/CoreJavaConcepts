package com.catmanscodes.collections.map.weakhashmap;

import java.util.Map;
import java.util.WeakHashMap;

public class WeakHashMapMain {

    public static void main(String[] args) {

        WeakHashMap<String, Integer> map = new WeakHashMap<>();

        fillTheMap(map);

        System.out.println("initial weekhashmap : " + map);

        System.gc();
        try {
            Thread.sleep(10000);
        } catch (Exception e) {
        }

        System.out.println("after gc weekhashmap : " + map);
    }

    private static void fillTheMap(Map<String, Integer> myMap) {
        String key1 = new String("Java");
        String key2 = new String("AI");

        myMap.put(key1, 25);
        myMap.put(key2, 10);

    }

}
