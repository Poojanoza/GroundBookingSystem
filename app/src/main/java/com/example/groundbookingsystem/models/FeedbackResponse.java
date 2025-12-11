package com.example.groundbookingsystem.models;

public class FeedbackResponse {
    public boolean success;
    public Feedback data;
    public String message;

    public FeedbackResponse() {}

    public static class Feedback {
        public String id;
        public String booking_id;
        public String user_id;
        public String ground_id;
        public int rating;
        public String comment;
        public String created_at;
        public String updated_at;

        public Feedback() {}
    }
}

