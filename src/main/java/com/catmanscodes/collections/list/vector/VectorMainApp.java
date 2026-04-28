package com.catmanscodes.collections.list.vector;

import java.util.Vector;

public class VectorMainApp {
    public static void main(String[] args) {

        //introduce on 1.0 version, it's thread safe , and good for multi threading applications.
        // it implemented RandomAccess so get element is fast here

        Vector<Integer> numVector = new Vector<>();

        numVector.add(11);
        numVector.add(2);
        numVector.add(31);
        numVector.add(11);
        numVector.add(0);
        numVector.add(7);
        System.out.println(numVector);

        numVector.capacity();
        numVector.remove(1);

        System.out.println(numVector.contains(11));

        numVector.clear();
        System.out.println(numVector);

    }
}
