package com.example.groundbookingsystem.models;

public class Feedback {
    public String id;
    public String booking_id;
    public String user_id;
    public String ground_id;
    public int rating;
    public String comment;
    public String created_at;
    public String updated_at;
    public UserInfo users;
    public BookingInfo bookings;

    public Feedback() {}

    public static class UserInfo {
        public String name;
        public String email;
    }

    public static class BookingInfo {
        public String booking_date;
        public String time_slot;
    }
}

