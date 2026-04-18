package com.hotel.osdl.service;

public class PaymentProcessingTask implements Runnable {
    @Override
    public void run() {
        int step = 1;
        while (step <= 2) {
            try {
                Thread.sleep(60);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
            step++;
        }
    }
}
