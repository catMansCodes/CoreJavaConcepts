package com.catmanscodes.java8.predicate;

import java.util.function.Predicate;

public class PredicateMainApp {
    public static void main(String[] args) {

        Predicate<Integer> isBigNumber = num1 -> num1 > 10;

        if (isBigNumber.test(11)) {
            System.out.println("yes it's big num");
        }

        Predicate<String> str = str1 -> str1.startsWith("V");
        Predicate<String> str2 = str1 -> str1.endsWith("l");

        Predicate<String> strResult = str.or(str2);

        if (strResult.test("Vimal")) {
            System.out.println("Yes it's start with V");
        }

    }
}
