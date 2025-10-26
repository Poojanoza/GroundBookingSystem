package com.example.groundbookingsystem.models;

public class Booking {
    private String id;
    private String userId;
    private String userName;
    private String userEmail;
    private String groundId;
    private String groundName;
    private String date;
    private String timeSlot;
    private double totalPrice;
    private String status;
    private long timestamp;

    public Booking() {
        // Required empty constructor for Firebase
    }

    public Booking(String id, String userId, String userName, String userEmail,
                   String groundId, String groundName, String date, String timeSlot,
                   double totalPrice, String status, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.groundId = groundId;
        this.groundName = groundName;
        this.date = date;
        this.timeSlot = timeSlot;
        this.totalPrice = totalPrice;
        this.status = status;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getGroundId() { return groundId; }
    public void setGroundId(String groundId) { this.groundId = groundId; }

    public String getGroundName() { return groundName; }
    public void setGroundName(String groundName) { this.groundName = groundName; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
