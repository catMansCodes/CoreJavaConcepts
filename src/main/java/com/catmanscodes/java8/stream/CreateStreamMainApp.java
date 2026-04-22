package com.catmanscodes.java8.stream;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CreateStreamMainApp {
    public static void main(String[] args) {

        int[] arr = {1,2,3,4,5};
        Arrays.stream(arr).forEach(System.out::println);

        List<String> strStream = Arrays.asList("vimal","java","spring");

        List<String> collect = strStream.stream().filter(str -> str.length() > 5).map(str -> str.toUpperCase()).collect(Collectors.toList());

        System.out.println(collect);
    }
}
