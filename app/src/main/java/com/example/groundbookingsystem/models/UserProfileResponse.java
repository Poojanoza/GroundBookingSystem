package com.example.groundbookingsystem.models;

public class UserProfileResponse {
    public boolean success;
    public UserProfile data;

    public UserProfileResponse() {}

    public static class UserProfile {
        public String id;
        public String name;
        public String email;
        public String phone;
        public String image_url;
        public String created_at;

        public UserProfile() {}
    }
}
