package com.catmanscodes.java8.suppiler;

import java.util.function.Supplier;

public class SuppilerMainApp {
    public static void main(String[] args) {

        Supplier<String> supStr = ()-> "hii";

        System.out.println(supStr.get());

    }
}
