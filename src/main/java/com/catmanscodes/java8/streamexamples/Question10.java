package com.catmanscodes.java8.streamexamples;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Question10 {
    public static void main(String[] args) {
        // common elements in bot list

        List<Integer> list1 = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        List<Integer> list2 = Arrays.asList(1, 5, 6, 9, 10);

        //best approach convert 1 list to set and set.contains bz set-> o(1)

        List<Integer> collect = list1.stream()
                .filter(element -> list2.contains(element))
                .collect(Collectors.toList());

        System.out.println(collect);

    }
}
