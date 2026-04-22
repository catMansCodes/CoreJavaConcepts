package com.catmanscodes.java8.defaultmethod;

public class CardPayment implements PaymentService{

    @Override
    public double pay(double amount){
        return amount + 10.0;
    }
}
