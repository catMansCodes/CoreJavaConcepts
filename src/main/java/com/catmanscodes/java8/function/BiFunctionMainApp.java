package com.catmanscodes.java8.function;

import java.util.function.BiFunction;

public class BiFunctionMainApp {
    public static void main(String[] args) {

        BiFunction<String,String,String> biFunction = (str,str2) -> {
            return str+" + "+str2;
        };

        System.out.println(biFunction.apply("vimal","maru"));
    }
}
