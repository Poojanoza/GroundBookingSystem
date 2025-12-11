package com.example.groundbookingsystem.models;

import java.io.Serializable;

public class User implements Serializable {
    public String id; // UUID
    public String name;
    public String email;
    public String phone;
    public boolean is_admin;

    public User() {}
}
