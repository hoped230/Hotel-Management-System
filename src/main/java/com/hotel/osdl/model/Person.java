package com.hotel.osdl.model;

import java.io.Serializable;

public abstract class Person implements Serializable {
    private final String id;
    private final String name;
    private final String phone;

    protected Person(String id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }
}
