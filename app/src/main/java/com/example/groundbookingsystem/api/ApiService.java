package com.example.groundbookingsystem.api;

import com.example.groundbookingsystem.models.*;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    @POST("/api/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("/api/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @GET("/api/grounds")
    Call<GroundsResponse> getGrounds();

    @GET("/api/bookings/{userId}")
    Call<BookingsResponse> getBookings(@Path("userId") String userId);

    @POST("/api/bookings")
    Call<Booking> createBooking(@Body Booking booking);
}
