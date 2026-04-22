package com.catmanscodes.java8.consumer;

import java.util.function.BiConsumer;

public class BiConsumerMainApp {

    public static void main(String[] args) {

        //take 2 argument no return just consume both
        BiConsumer<String, String> biConsumer = (str1, str2) -> System.out.println(str1+" "+str2);
        biConsumer.accept("vimal","maru");

    }
}
