package com.catmanscodes.java8.consumer;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ConsumerMainApp {
    public static void main(String[] args) {

        //consumer will take and accept no return anything
        Consumer<String> strConsumer = str -> System.out.println(str);
        strConsumer.accept("Hello There");

        Consumer<List<String>> nameList = names -> {
            names.forEach(name-> System.out.println(name));
        };

        Consumer<List<String>> nameList2 = names -> {
            names.forEach(name-> System.out.println(name+" * "));
        };

        nameList.andThen(nameList2).accept(Arrays.asList("vimal","java","catman"));

    }
}
