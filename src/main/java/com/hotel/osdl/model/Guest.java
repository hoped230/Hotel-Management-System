package com.hotel.osdl.model;

import java.io.Serializable;

// Feature used: inheritance from the abstract Person class.
public class Guest extends Person implements Serializable {
    private String email;

    public Guest(String id, String name, String phone, String email) {
        super(id, name, phone);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
