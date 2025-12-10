package com.example.groundbookingsystem.models;

public class ProfileUpdateRequest {
    public String name;
    public String phone;
    public String image_url;

    public ProfileUpdateRequest(String name, String phone, String image_url) {
        this.name = name;
        this.phone = phone;
        this.image_url = image_url;
    }

    public ProfileUpdateRequest() {}
}
