package com.hotel.osdl.service;

public class DisplayHelper {
    private DisplayHelper() {
    }

    // Feature used: generic method.
    public static <T> String display(T value) {
        return String.valueOf(value);
    }

    // Feature used: generic array method.
    public static <T> String printArray(T[] values) {
        StringBuilder builder = new StringBuilder();
        int index = 0;
        while (index < values.length) {
            builder.append(values[index]);
            if (index < values.length - 1) {
                builder.append(", ");
            }
            index++;
        }
        return builder.toString();
    }
}
