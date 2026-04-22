package com.catmanscodes.java8.defaultmethod;

public interface PaymentService {

    String paymentType = "B2B";

    public abstract double pay(double amount);

    default void type(){
        System.out.println(paymentType);
    }

    static void paymentMode(){
        System.out.println("Bank Transfer");
    }
}
