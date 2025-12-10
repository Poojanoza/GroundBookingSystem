package com.example.groundbookingsystem.api;

import com.example.groundbookingsystem.models.*;
import retrofit2.Call;
import retrofit2.http.*;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public interface ApiService {
    // Auth Endpoints
    @POST("/api/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("/api/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    // Ground Endpoints
    @GET("/api/grounds")
    Call<GroundsResponse> getGrounds();

    @GET("/api/grounds/{groundId}")
    Call<GroundDetailResponse> getGroundDetails(@Path("groundId") String groundId);

    @GET("/api/grounds/{groundId}/available-slots/{date}")
    Call<SlotsResponse> getAvailableSlots(
            @Path("groundId") String groundId,
            @Path("date") String date
    );

    // Booking Endpoints
    @POST("/api/bookings")
    Call<BookingResponse> createBooking(
            @Header("Authorization") String token,
            @Body BookingRequest booking
    );

    @GET("/api/bookings/active/{userId}")
    Call<BookingsResponse> getActiveBookings(
            @Path("userId") String userId,
            @Header("Authorization") String token
    );

    @GET("/api/bookings/history/{userId}")
    Call<BookingsResponse> getBookingHistory(
            @Path("userId") String userId,
            @Header("Authorization") String token
    );

    @PUT("/api/bookings/{bookingId}/cancel")
    Call<CancelBookingResponse> cancelBooking(
            @Path("bookingId") String bookingId,
            @Header("Authorization") String token
    );

    // User Profile Endpoints
    @GET("/api/user/profile")
    Call<UserProfileResponse> getUserProfile(
            @Header("Authorization") String token
    );

    @PUT("/api/user/profile")
    Call<UserProfileResponse> updateProfile(
            @Header("Authorization") String token,
            @Body ProfileUpdateRequest request
    );

    @POST("/api/user/change-password")
    Call<MessageResponse> changePassword(
            @Header("Authorization") String token,
            @Body ChangePasswordRequest request
    );

    // Admin Endpoints
    @GET("/api/admin/bookings")
    Call<AdminBookingsResponse> getAdminBookings(
            @Header("Authorization") String token
    );

    @GET("/api/admin/bookings-summary")
    Call<AdminBookingsResponse> getBookingsSummary(
            @Header("Authorization") String token
    );
    
    @GET("/api/admin/statistics")
    Call<AdminStatsResponse> getAdminStatistics(
            @Header("Authorization") String token
    );

    @GET("/api/admin/grounds/{groundId}/bookings")
    Call<AdminBookingsResponse> getGroundBookings(
            @Path("groundId") String groundId,
            @Header("Authorization") String token
    );

    @GET("/api/admin/bookings-by-date-range")
    Call<AdminBookingsResponse> getBookingsByDateRange(
            @Query("startDate") String startDate,
            @Query("endDate") String endDate,
            @Header("Authorization") String token
    );

    // Ground Creation Endpoints (Admin)
    @POST("/api/grounds")
    Call<GroundDetailResponse> createGround(
            @Header("Authorization") String token,
            @Body CreateGroundRequest request
    );

    @Multipart
    @POST("/api/grounds/upload-image")
    Call<ImageUploadResponse> uploadGroundImage(
            @Header("Authorization") String token,
            @Part MultipartBody.Part image
    );

    // Ground Management Endpoints (Admin)
    @GET("/api/admin/grounds")
    Call<GroundsResponse> getAdminGrounds(
            @Header("Authorization") String token
    );

    @PUT("/api/grounds/{groundId}")
    Call<GroundDetailResponse> updateGround(
            @Path("groundId") String groundId,
            @Header("Authorization") String token,
            @Body CreateGroundRequest request
    );

    @DELETE("/api/grounds/{groundId}")
    Call<MessageResponse> deleteGround(
            @Path("groundId") String groundId,
            @Header("Authorization") String token
    );
}
