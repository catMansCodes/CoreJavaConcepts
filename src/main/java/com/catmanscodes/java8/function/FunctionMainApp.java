package com.catmanscodes.java8.function;

import java.util.function.Function;

public class FunctionMainApp {
    public static void main(String[] args) {

        Function<String, String> fun = (str1) -> {
            return str1 + "-123";
        };

        System.out.println(fun.apply("password"));
    }
}
