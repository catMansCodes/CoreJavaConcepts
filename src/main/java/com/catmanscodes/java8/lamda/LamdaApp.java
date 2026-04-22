package com.catmanscodes.java8.lamda;

public class LamdaApp {
    public static void main(String[] args) {

        CardPayment cardPayment = () -> "Credit Card";
        System.out.println(cardPayment.getCardDetails());

        Runnable runnable = () -> {
            for(int i=1;i<=10;i++){
                System.out.println("hii .. "+i);
            }
        };

        runnable.run();

    }
}
