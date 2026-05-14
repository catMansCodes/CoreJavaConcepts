package com.catmanscodes.java8.streamexamples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Question4 {
    public static void main(String[] args) {

        // 2nd highest number

        List<Integer> list = new ArrayList<>(Arrays.asList(20, 10, 10, 30, 45, 5, 20));

        list.stream()
                .distinct()
                .sorted((a, b) -> (b - a))
                .skip(1)
                .limit(1)
                .forEach(System.out::println);

    }
}
