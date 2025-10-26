package com.example.groundbookingsystem.models;

public class Ground {
    private String id;
    private String name;
    private String location;
    private String description;
    private String imageUrl;
    private double pricePerHour;
    private String type;

    public Ground() {
        // Required empty constructor for Firebase
    }

    public Ground(String id, String name, String location, String description,
                  String imageUrl, double pricePerHour, String type) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.description = description;
        this.imageUrl = imageUrl;
        this.pricePerHour = pricePerHour;
        this.type = type;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public double getPricePerHour() { return pricePerHour; }
    public void setPricePerHour(double pricePerHour) { this.pricePerHour = pricePerHour; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
