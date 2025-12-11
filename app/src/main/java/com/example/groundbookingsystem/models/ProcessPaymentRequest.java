package com.example.groundbookingsystem.models;

public class ProcessPaymentRequest {
    public String payment_id;
    public String stripe_payment_intent_id; // Optional: Stripe payment intent ID

    public ProcessPaymentRequest(String payment_id) {
        this.payment_id = payment_id;
    }

    public ProcessPaymentRequest(String payment_id, String stripe_payment_intent_id) {
        this.payment_id = payment_id;
        this.stripe_payment_intent_id = stripe_payment_intent_id;
    }
}

