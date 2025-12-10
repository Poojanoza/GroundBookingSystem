package com.example.groundbookingsystem.models;

public class ChangePasswordRequest {
    public String oldPassword;
    public String newPassword;

    public ChangePasswordRequest(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    public ChangePasswordRequest() {}
}
