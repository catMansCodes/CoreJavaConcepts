package com.catmanscodes.collections.list.arraylist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ArrayListMainApp {
    public static void main(String[] args) {

        //1. Create Array List

        //1. integer ArrayList
        List<Integer> numList = new ArrayList<>();
        numList.add(11);
        numList.add(1);
        numList.add(21);
        numList.add(0);
        numList.add(1);
        numList.add(17);

        System.out.println("Number List: " + numList);

        //2. String ArrayList
        List<String> nameList = new ArrayList<>();

        nameList.add("Java");
        nameList.add("Spring");
        nameList.add("Aws");
        nameList.add("Hibernate");
        nameList.add("JDBC");
        nameList.add("MYSQL");
        nameList.add("Docker");

        nameList.sort((a,b)-> (b.length() - a.length()));

        System.out.println("String List: " + nameList);


        //3. Object ArrayList
        List<User> userList = new ArrayList<>();

        userList.add(new User(1, "Jarvis", "jarvis@gmail.com"));
        userList.add(new User(3, "john", "john@gmail.com"));
        userList.add(new User(2, "philip", "php@gmail.com"));
        userList.add(new User(11, "catman", "catman@gmail.com"));
        userList.add(new User(121, "Java", "java@gmail.com"));
        System.out.println(userList);

        //2. Arrays.asList() vs List.of()

        //Arrays.asList(): modify element but can't add or remove or change it size

        List<Integer> newIntList = Arrays.asList(1, 3, 4, 2, 6, 73, 6, 55, 8, 99, 0, 2);
        newIntList.set(1, 11); // replace 3 with 11 index 1
        //newIntList.add(10);//Runtime Error can't change this list
        System.out.println("Arrays.asList: " + newIntList);

        //List.of(): Immutable java 9 list feature can't modify the element or change anything.
        List<Integer> newListof = List.of(22, 1, 3, 66, 7, 8, 9, 0, 1, 1);
        System.out.println("newListof: " + newListof);
        //newListof.add(12); //immutable list can't change anything

        //adding collection inside the constructor.
        List<String> appData = new ArrayList<>(List.of("java","spring","Mysql"));
        appData.add("Angular"); //this will work as we are dealing with the Constructor
        System.out.println(appData);

        //3. Array List Methods

        List<String> userNames = new ArrayList<>(Arrays.asList("Tonny","Stark","Jarvis"));
        userNames.add("Captain");
        userNames.set(1,"Mr.Stark");
        userNames.remove("Captain");

        Collections.sort(userNames); //sort in asc

        System.out.println(userNames);
        System.out.println(userNames.size());
        System.out.println(userNames.contains("Jarvis"));

        //4. Time Complexity

       // get(index)           : O(1)  // fast
       // set(index, value)    : O(1)  // fast

        //add(element)         : O(1) amortized, O(n) worst (resize)
       // add(index, element)  : O(n)  // shifting

        //remove(index)        : O(n)  // shifting
       // remove(last element) : O(1)

    }
}
