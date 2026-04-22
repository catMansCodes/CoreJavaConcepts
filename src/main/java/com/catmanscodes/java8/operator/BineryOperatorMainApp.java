package com.catmanscodes.java8.operator;

import java.util.function.BinaryOperator;

public class BineryOperatorMainApp {
    public static void main(String[] args) {

        BinaryOperator<String> binaryOperator = (str1, str2) -> str1 + str2;

        System.out.println(binaryOperator.apply("vimal","maru"));


    }
}
