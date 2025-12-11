package com.example.groundbookingsystem.models;

public class FeedbackRequest {
    public String booking_id;
    public int rating; // 1-5
    public String comment;

    public FeedbackRequest(String booking_id, int rating, String comment) {
        this.booking_id = booking_id;
        this.rating = rating;
        this.comment = comment;
    }
}

