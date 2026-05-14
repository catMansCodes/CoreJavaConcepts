package com.catmanscodes.multithreading;

public class Counter {
    int count = 0;

    public synchronized void increaseCount(){
        this.count++;
    }
}
