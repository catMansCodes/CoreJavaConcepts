package com.catmanscodes.collections.set;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

public class SetAllMain {
    public static void main(String[] args) {

        // Set(I) -> HashSet, LinkedHashSet, TreeSet, EnumSet
        // No duplication

        //1 HasSet: like hashmap , no sorting
        Set<Integer> intSet = new HashSet<>();

        intSet.add(1);
        intSet.add(21);
        intSet.add(10);
        intSet.add(3);
        intSet.add(107);
        intSet.add(41);

        System.out.println(intSet);

        //2 LinkedHashSet : same insertion order

        LinkedHashSet<String> strSet = new LinkedHashSet<>();

        strSet.add("Java");
        strSet.add("Spring");
        strSet.add("Mysql");
        System.out.println(strSet);

        //3 TreeSet - insertion order

        TreeSet<String> strTreeSet = new TreeSet<>();

        strTreeSet.add("Java");
        strTreeSet.add("AWS");
        strTreeSet.add("Spring");
        strTreeSet.add("Mysql");
        strTreeSet.add("AI");

        System.out.println(strTreeSet);


    }

}
