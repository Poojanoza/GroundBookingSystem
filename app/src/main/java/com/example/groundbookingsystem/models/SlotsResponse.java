package com.example.groundbookingsystem.models;

import java.util.List;

public class SlotsResponse {
    public boolean success;
    public List<String> availableSlots;
    public List<String> bookedSlots;

    public SlotsResponse() {}
}
