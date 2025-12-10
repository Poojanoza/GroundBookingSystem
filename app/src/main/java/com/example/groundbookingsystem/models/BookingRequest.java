package com.example.groundbookingsystem.models;

public class BookingRequest {
    public String ground_id;
    public String booking_date;
    public String time_slot;
    public double price;

    public BookingRequest(String ground_id, String booking_date, String time_slot, double price) {
        this.ground_id = ground_id;
        this.booking_date = booking_date;
        this.time_slot = time_slot;
        this.price = price;
    }

    public BookingRequest() {}
}
