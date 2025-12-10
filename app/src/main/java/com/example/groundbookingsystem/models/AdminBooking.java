package com.example.groundbookingsystem.models;

public class AdminBooking {
    public String id;
    public String ground_id;
    public String booking_date;
    public String time_slot;
    public double price;
    public String status;
    public BookingUser users;
    public BookingGround grounds;

    public AdminBooking() {}

    public static class BookingUser {
        public String name;
        public String email;
        public String phone;

        public BookingUser() {}
    }

    public static class BookingGround {
        public String id;
        public String name;
        public String location;
        public String type;

        public BookingGround() {}
    }
}
