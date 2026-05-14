package com.catmanscodes.java8.streamexamples;

import java.util.*;
import java.util.stream.Collectors;

public class Question1 {
    public static void main(String[] args) {

        //Given a list of integers, remove duplicates and sort them in descending order.

        List<Integer> list = new ArrayList<>(Arrays.asList(5, 3, 1, 3, 2, 5, 4));

        List<Integer> collect = list.stream()
                .distinct()
                .sorted((a, b) -> (b - a))
                .collect(Collectors.toList());

        System.out.println(collect);

    }
}
