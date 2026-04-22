package com.catmanscodes.java8.dateandtime;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class DateMainApp {
    public static void main(String[] args) {

        //1. LocalDate
        //2. LocalTime
        //3. LocalDateAndTime
        //4. ZoneDateTime
        //5. Instance
        //6. Duration
        //7. Period
        //8. DateTimeFormator

        //1. LocalDate
        LocalDate today = LocalDate.now();
        System.out.println(today);

        //2. LocalTime
        LocalTime time = LocalTime.now();
        System.out.println(time);

        //3. LocalDateAndTime
        LocalDateTime dateAndTime = LocalDateTime.now();
        System.out.println(dateAndTime);

        //4. ZoneDateTime
        ZonedDateTime zone = ZonedDateTime.now();
        System.out.println(zone.getZone());

        ZonedDateTime chicagoCurrentTIme = ZonedDateTime.now(ZoneId.of("America/Chicago"));
        ZonedDateTime indiaCurrentTIme = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));

        System.out.println(chicagoCurrentTIme);
        System.out.println(indiaCurrentTIme);

        //5. Instance
        Instant instant = Instant.now();
        System.out.println(instant);

        //6. Duration : for small duration difference find

        LocalDateTime start = LocalDateTime.now();
        for(int i =1; i<=100000;i++){}
        LocalDateTime end = LocalDateTime.now();

        Duration duration = Duration.between(start,end);
        System.out.println(duration);

        //7. Period : for long time difference find
        LocalDate recent  = LocalDate.now();
        LocalDate past = LocalDate.of(1993,11,1);

        Period diff = Period.between(recent,past);
        System.out.println(diff);

        //8. DateTimeFormator

        String birthDate = "01/11/1993";

        DateTimeFormatter formate = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate date = LocalDate.parse(birthDate, formate);
        System.out.println(date);

        String newDate = "20/04/2026 16:41:59";

        DateTimeFormatter newFormate = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalDateTime formatedDate = LocalDateTime.parse(newDate,newFormate);
        System.out.println(formatedDate);


    }
}
