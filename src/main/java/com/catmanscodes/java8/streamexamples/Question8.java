package com.catmanscodes.java8.streamexamples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Question8 {
    public static void main(String[] args) {

        // Find the frequency of each element
        // TIP: key-> number && value->count

        List<Integer> list = new ArrayList<>(Arrays.asList(3, 2, 3, 4, 4, 1, 2, 1, 1, 1, 5, 6, 5));

        Map<Integer, Long> collect = list.stream().collect(Collectors.groupingBy(
                num -> num, Collectors.counting()
        ));

        System.out.println(collect);
    }
}
