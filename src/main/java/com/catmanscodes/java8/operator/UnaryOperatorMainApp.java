package com.catmanscodes.java8.operator;

import java.util.function.UnaryOperator;

public class UnaryOperatorMainApp {
    public static void main(String[] args) {

        UnaryOperator<String> unaryOperator = str->str.toUpperCase();

        System.out.println(unaryOperator.apply("java"));
    }
}
