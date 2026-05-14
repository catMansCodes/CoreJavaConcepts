package com.catmanscodes.java8.streamexamples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Question3 {
    public static void main(String[] args) {

        // Get 2nd & 3rd element

        List<Integer> list = new ArrayList<>(Arrays.asList(10, 20, 30, 40, 50));

        List<Integer> collect = list.stream()
                .skip(1)
                .limit(2)
                .collect(Collectors.toList());

        System.out.println(collect);
    }
}
