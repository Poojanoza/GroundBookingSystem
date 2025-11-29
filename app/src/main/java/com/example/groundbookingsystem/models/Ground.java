package com.example.groundbookingsystem.models;

import java.io.Serializable;

public class Ground implements Serializable {
    public String id;
    public String name;
    public String location;
    public String type;
    public String description;
    public double price;
    public String image_url;

    public Ground() {}
}
