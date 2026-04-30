package com.catmanscodes.collections.list.stack;

import java.util.Stack;

public class StackMainApp {
    public static void main(String[] args) {

        Stack<Integer> stackList = new Stack<>();
        // add, push both will work due to vector is parent here
        stackList.add(21);
        stackList.add(7);
        stackList.add(1);
        stackList.add(0);
        stackList.add(null);
        stackList.add(11);
        stackList.add(1);

        System.out.println("original order: " + stackList);

        System.out.println("print TOP element using peek: " + stackList.peek());
        System.out.println("search null it will return 3 LIFO: " + stackList.search(null));
        stackList.push(4);
        stackList.push(33);
        System.out.println("new stack " + stackList);
        stackList.pop();
        System.out.println("after pop TOP will remove" + stackList);

    }
}
