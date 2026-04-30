package com.catmanscodes.collections.list.vector;

import java.util.Vector;

public class VectorMainApp {
    public static void main(String[] args) {

        //1. it is introduced on legacy 1.0 version
        //2. it's sync & thread safe & good for multi threading applications.
        //3. it implemented RandomAccess so get element is fast here
        //4. it is dynamic array that grow automatically
        //5. double capacity when it up to max size


        Vector<Integer> numVector = new Vector<>();

        numVector.add(11);
        numVector.add(2);
        numVector.add(31);
        numVector.add(11);
        numVector.add(0);
        numVector.add(7);
        System.out.println("Initial vector list: "+numVector);

        System.out.println("capacity:"+numVector.capacity());

        numVector.remove(1);

        System.out.println("it contains no 11: "+numVector.contains(11));

        numVector.clear();

        System.out.println("after clear it:"+numVector);

    }
}
