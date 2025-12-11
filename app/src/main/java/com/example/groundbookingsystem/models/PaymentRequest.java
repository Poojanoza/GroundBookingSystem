package com.example.groundbookingsystem.models;

public class PaymentRequest {
    public String booking_id;
    public double amount;
    public String payment_method; // 'card', 'upi', 'wallet'

    public PaymentRequest(String booking_id, double amount, String payment_method) {
        this.booking_id = booking_id;
        this.amount = amount;
        this.payment_method = payment_method;
    }
}

