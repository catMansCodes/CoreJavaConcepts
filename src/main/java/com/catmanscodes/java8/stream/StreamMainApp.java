package com.catmanscodes.java8.stream;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StreamMainApp {
    public static void main(String[] args) {

        List<Integer> intList = Arrays.asList(1, 3, 4, 2, 7, 5, 8, 9, 10, 6);

        List<Integer> evenNoList = intList.stream()
                .filter(i -> i % 2 == 0)
                .map(no -> no / 2)
                .sorted((a, b) -> b - a)
                .collect(Collectors.toList());

        System.out.println(evenNoList);
    }
}
