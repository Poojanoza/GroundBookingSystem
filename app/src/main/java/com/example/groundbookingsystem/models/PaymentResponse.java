package com.example.groundbookingsystem.models;

public class PaymentResponse {
    public boolean success;
    public Payment data;
    public String message;
    public String transaction_id;
    public String client_secret; // Stripe client secret for PaymentSheet
    public String stripe_payment_intent_id; // Stripe payment intent ID

    public PaymentResponse() {}

    public static class Payment {
        public String id;
        public String booking_id;
        public String user_id;
        public double amount;
        public String payment_method;
        public String status;
        public String transaction_id;
        public String created_at;
        public String updated_at;

        public Payment() {}
    }
}

