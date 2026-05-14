package com.catmanscodes.java8.streamexamples;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Question11 {
    public static void main(String[] args) {
        // convert 1 single unique element list

        List<List<Integer>> listofList = Arrays.asList(
                Arrays.asList(1, 2, 3, 4),
                Arrays.asList(3, 4, 5, 6),
                Arrays.asList(7, 8, 1, 2),
                Arrays.asList(9, 5, 10, 6),
                Arrays.asList(11, 12, 3, 4),
                Arrays.asList(11, 12, 13, 14)
        );

        List<Integer> collect = listofList
                .stream()
                .flatMap(innerList -> innerList.stream()).distinct()
                .collect(Collectors.toList());

        System.out.println(collect);

    }
}
