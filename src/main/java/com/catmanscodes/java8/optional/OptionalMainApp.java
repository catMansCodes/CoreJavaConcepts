package com.catmanscodes.java8.optional;

import java.util.Optional;

public class OptionalMainApp {
    public static void main(String[] args) {

        Optional<String> say = getData();

        if (say.isPresent()) {
            String name = say.get();
            System.out.println(name);
        }
    }

    private static Optional<String> getData() {
        return Optional.of("hello");
    }
}
