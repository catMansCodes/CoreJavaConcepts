package com.catmanscodes.java8.streamexamples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Question2 {
    public static void main(String[] args) {

        // Find all the odd numbers abd return their squares

        List<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));

        List<Integer> collect = list.stream()
                .filter(num -> num % 2 != 0)
                .map(num -> num * num)
                .collect(Collectors.toList());

        System.out.println(collect);
    }
}
