package com.catmanscodes.java8.streamexamples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Question9 {
    public static void main(String[] args) {

        //Transaction amount per category

        List<Transaction> transactions = new ArrayList<>(
                Arrays.asList(
                        new Transaction("Food", 600),
                        new Transaction("Food", 200),
                        new Transaction("Food", 400),
                        new Transaction("Shopping", 5300),
                        new Transaction("Shopping", 2250),
                        new Transaction("Shopping", 1100),
                        new Transaction("Bill", 1200),
                        new Transaction("Bill", 650),
                        new Transaction("Bill", 400),
                        new Transaction("Bill", 500),
                        new Transaction("Movie", 300),
                        new Transaction("Movie", 600),
                        new Transaction("Travel", 4000),
                        new Transaction("Travel", 1000)
                )
        );

        Map<String, Integer> collect = transactions
                .stream()
                .collect(Collectors.groupingBy(trn -> trn.getCategory(),
                        Collectors.summingInt(trn->trn.getAmount())));
        System.out.println(collect);


    }
}
