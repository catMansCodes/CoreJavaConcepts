package com.catmanscodes.java8.lamda.compare;

import java.util.*;

public class CompareMainApp {
    public static void main(String[] args) {

        //list
        List<Integer> numList = new ArrayList<>(5);

        numList.add(10);
        numList.add(11);
        numList.add(2);
        numList.add(12);
        numList.add(1);
        numList.add(22);

        //ascending order
        numList.sort((a, b) -> a - b);
        System.out.println("asc: " + numList);
        //descending order
        numList.sort((a, b) -> b - a);

        System.out.println("desc: " + numList);

        //set

        Set<Integer> numSet = new TreeSet<>((a, b) -> b - a);

        numSet.add(11);
        numSet.add(12);
        numSet.add(10);
        numSet.add(3);
        numSet.add(21);
        numSet.add(101);
        numSet.add(114);
        numSet.add(112);

        System.out.println(numSet);

        //map

        Map<String, Integer> numMap = new TreeMap<>();

        numMap.put("a", 22);
        numMap.put("b", 2);
        numMap.put("c", 12);
        numMap.put("d", 32);
        numMap.put("e", 21);
        numMap.put("f", 1);
        numMap.put("g", 0);

        Map<Integer, String> mySortedMap = new TreeMap<>((a, b) -> b - a);

        for (Map.Entry<String, Integer> myMap : numMap.entrySet()) {
            System.out.println(myMap.getKey() + "-" + myMap.getValue());

            mySortedMap.put(myMap.getValue(), myMap.getKey());
        }

        System.out.println(numMap);

        System.out.println(mySortedMap);
    }

}
