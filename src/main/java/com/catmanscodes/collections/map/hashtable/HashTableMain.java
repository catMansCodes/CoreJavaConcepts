package com.catmanscodes.collections.map.hashtable;

import java.util.Hashtable;

public class HashTableMain {
    public static void main(String[] args) {

        //Legacy 1.0 - all methods sync including get so poor performance
        // replacement ConcurrentHashMap

        Hashtable<String, Integer> map = new Hashtable<>();

        map.put("java", 26);
        map.put("spring", 20);
        map.put("mysql", 8);
        map.put("ai", 2);

        System.out.println(map);

    }
}
