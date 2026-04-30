package com.catmanscodes.collections.list.copyonwritearraylist;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CopyOnWriteArrayListMain {
    public static void main(String[] args) {

//Exception in thread "main" java.util.ConcurrentModificationException for ArrayList
        //List<String> glosaryList = new ArrayList<>();

        List<String> glosaryList = new CopyOnWriteArrayList<>();

        glosaryList.add("Milk");
        glosaryList.add("Bread");
        glosaryList.add("Banana");

        System.out.println("original list:"+ glosaryList);

        glosaryList.add("Chess");

        for(String item : glosaryList){
            System.out.println(item);

            if (item =="Bread"){
                glosaryList.add("Pineapple");
                System.out.println("sweet sandwich party!!");
            }
        }

        System.out.println("Final list: "+glosaryList); // will add Pineapple at last
    }
}
