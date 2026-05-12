package com.catmanscodes.oops;

public class MainApp {

    public static void main(String[] args) {
        Employee e1 = new Employee();
        Employee e2 = new Employee();

        e1.setId(1);
        e2.setId(1);

        e1.setName("John");
        e2.setName("John");

        System.out.println(e1.equals(e2));


    }

}
