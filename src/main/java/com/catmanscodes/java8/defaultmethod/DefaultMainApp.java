package com.catmanscodes.java8.defaultmethod;

public class DefaultMainApp {

    public static void main(String[] args) {

        PaymentService cardPayment = new CardPayment();

        System.out.println(cardPayment.pay(20));

        cardPayment.type();

        PaymentService.paymentMode();
    }
}
