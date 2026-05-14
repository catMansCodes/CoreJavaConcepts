package com.catmanscodes.multithreading;

public class MainApp {

    public static void main(String[] args) throws InterruptedException {

        Counter counter = new Counter();

        Thread t1 = new Thread(
                () -> {
                    for (int i = 0; i < 10000; i++) {
                        counter.increaseCount();
                    }
                });

        Thread t2 = new Thread(
                () -> {
                    for (int i = 0; i < 10000; i++) {
                        counter.increaseCount();
                    }
                });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println(counter.count);

    }


}
