package com.example.groundbookingsystem.models;

import java.io.Serializable;

public class Booking implements Serializable {
    public String id;
    public String user_id;
    public String ground_id;
    public String booking_date;
    public String time_slot;
    public String status;
    public double price;
    public String payment_id;
    public String payment_status;
    public Ground grounds; // API returns as 'grounds'
    public Ground ground; // Fallback compatibility
    public User user;
    
    // For adapter access compatibility
    public String getDate() { return booking_date; }
    public String getSlot() { return time_slot; }

    public Booking() {}
}
