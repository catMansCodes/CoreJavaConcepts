package com.catmanscodes.collections.map.sortedmap;

import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;

public class SortedMapMain {
    public static void main(String[] args) {

        //maintain the sorting order
        SortedMap<String, Integer> map = new TreeMap<>();
        map.put("java", 26);
        map.put("spring", 7);
        map.put("mysql", 8);
        map.put("aws", 11);

        System.out.println(map); //ascending order

        SortedMap<String, Integer> map2 = new TreeMap<>(Comparator.reverseOrder()); // can give comparator in const here

        map2.put("java", 26);
        map2.put("spring", 7);
        map2.put("mysql", 8);
        map2.put("aws", 11);
        map2.put("aop", 11);

        System.out.println(map2); //descending order

    }
}
