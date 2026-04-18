package com.hotel.osdl.service;

// Feature used: bounded type parameter.
public class NumberCalculator<T extends Number> {
    private T first;
    private T second;

    public NumberCalculator(T first, T second) {
        this.first = first;
        this.second = second;
    }

    public Double add() {
        return Double.valueOf(first.doubleValue() + second.doubleValue());
    }

    public Double subtract() {
        return Double.valueOf(first.doubleValue() - second.doubleValue());
    }
}
