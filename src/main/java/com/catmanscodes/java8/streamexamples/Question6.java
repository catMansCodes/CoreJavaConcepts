package com.catmanscodes.java8.streamexamples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Question6 {
    public static void main(String[] args) {

        // Find the longest string in list

        List<String> list = new ArrayList<>(Arrays.asList("Java", "jsp", "SpringRoot", "Spring", "Aws", "SpringBoot"));

        List<String> collect = list.stream()
                .sorted((str1, str2) -> str2.length() - str1.length())
                .collect(Collectors.toList());

        List<String> newList = new ArrayList<>();

        if (collect.size() > 1) {
            list.stream().forEach((str) -> {
                if (collect.get(1).length() == str.length()) {

                    newList.add(str);
                }
            });
        }

        System.out.println(newList);
    }
}
