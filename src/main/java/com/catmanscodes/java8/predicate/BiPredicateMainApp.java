package com.catmanscodes.java8.predicate;

import java.util.function.BiPredicate;

public class BiPredicateMainApp {
    public static void main(String[] args) {

        BiPredicate<Integer, Integer> biPredicate = (i, j) -> i > j;

        System.out.println(biPredicate.test(11, 21));
    }
}
