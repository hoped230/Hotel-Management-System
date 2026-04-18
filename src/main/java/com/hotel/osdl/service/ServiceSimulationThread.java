package com.hotel.osdl.service;

public class ServiceSimulationThread extends Thread {
    private String taskName;

    public ServiceSimulationThread(String taskName) {
        this.taskName = taskName;
    }

    @Override
    public void run() {
        int step = 1;
        while (step <= 2) {
            Thread.yield();
            try {
                Thread.sleep(60);
            } catch (InterruptedException exception) {
                interrupt();
            }
            step++;
        }
    }
}
