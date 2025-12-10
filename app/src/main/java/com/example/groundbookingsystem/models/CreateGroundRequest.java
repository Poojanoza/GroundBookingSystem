package com.example.groundbookingsystem.models;

public class CreateGroundRequest {
    public String name;
    public String location;
    public String type;
    public String description;
    public double price;
    public String image_url;

    public CreateGroundRequest(String name, String location, String type, String description, double price, String image_url) {
        this.name = name;
        this.location = location;
        this.type = type;
        this.description = description;
        this.price = price;
        this.image_url = image_url;
    }
}
