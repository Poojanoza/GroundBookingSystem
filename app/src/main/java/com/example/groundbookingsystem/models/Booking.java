package com.example.groundbookingsystem.models;
public class Booking {
    public String id;
    public String user_id;
    public String ground_id;
    public String booking_date;
    public String time_slot;
    public String status;
    public double price;
    public Ground ground; // Optional, if you embed ground info with booking

    public Booking() {}
}
