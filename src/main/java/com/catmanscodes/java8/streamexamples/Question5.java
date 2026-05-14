package com.catmanscodes.java8.streamexamples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Question5 {
    public static void main(String[] args) {

        // Divide numbers in to even & odd.
        // TIP: partition - 2 result

        List<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));

        Map<Boolean, List<Integer>> collect = list
                .stream()
                .collect(Collectors.partitioningBy(x -> x % 2 == 0));

        System.out.println(collect);


    }
}
