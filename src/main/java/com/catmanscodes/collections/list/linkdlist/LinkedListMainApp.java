package com.catmanscodes.collections.list.linkdlist;

import java.util.LinkedList;

public class LinkedListMainApp {
    public static void main(String[] args) {

        //List<Integer> nmList = new LinkedList<>();

        //if we use List to hold the reference we can't use the LinkedList implemented methods
        //so used new LinkedList<>() here

        LinkedList<Integer> numList = new LinkedList<>();

        numList.add(1);
        numList.add(2);
        numList.add(3);
        numList.add(33);
        numList.add(1);
        numList.add(4);
        numList.add(6);

        System.out.println("Original List: " + numList);

        numList.remove(4);
        System.out.println("Removed index 4: " + numList);

        numList.addFirst(111); //O(1)
        numList.getFirst(); // O(1)
        numList.remove(1);
        System.out.println("after add & remove " + numList);

        System.out.println("size: " + numList.size());

        System.out.println("get 5th index: " + numList.get(5)); // O(n)
        System.out.println("final list" + numList);

        System.out.println("print linked list +1 using foreach loop ==>>");
        numList.forEach(num ->
                {
                    num = num+1;
                    System.out.println(num);
                });

    }
}
