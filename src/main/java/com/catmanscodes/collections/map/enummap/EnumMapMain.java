package com.catmanscodes.collections.map.enummap;

import java.util.EnumMap;
import java.util.Map;

public class EnumMapMain {
    public static void main(String[] args) {

        // no hashing
        // order as per the enum index / ordinal
        // fast then hashmap
        // array of size as per enum

        Map<Day, String> map = new EnumMap<>(Day.class);

        map.put(Day.MONDAY, "GYM");
        map.put(Day.SUNDAY, "SLEEP");
        map.put(Day.TUESDAY, "YOGA");

        System.out.println(map.get(Day.SATURDAY)); // null

        System.out.println(map);

    }
}

enum Day {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY
}
