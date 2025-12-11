package com.example.groundbookingsystem.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.groundbookingsystem.FeedbackDialog;
import com.example.groundbookingsystem.R;
import com.example.groundbookingsystem.api.ApiClient;
import com.example.groundbookingsystem.api.ApiService;
import com.example.groundbookingsystem.models.Booking;
import com.example.groundbookingsystem.models.CancelBookingResponse;
import com.example.groundbookingsystem.models.Ground;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {

    private List<Booking> bookings;
    private Context context;
    private ApiService apiService;
    private String token;

    public BookingAdapter(List<Booking> bookings, Context context) {
        this.bookings = bookings != null ? bookings : new java.util.ArrayList<>();
        this.context = context;
        
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        token = prefs.getString("token", "");
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context ctx = context != null ? context : parent.getContext();
        View view = LayoutInflater.from(ctx).inflate(R.layout.item_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (bookings == null || position < 0 || position >= bookings.size() || context == null) {
            return;
        }

        Booking booking = bookings.get(position);
        if (booking == null) {
            return;
        }

        Ground ground = booking.grounds != null ? booking.grounds : booking.ground;
        
        // Set ground name
        if (holder.name != null) {
            holder.name.setText(ground != null && ground.name != null ? ground.name : "Ground");
        }

        // Set booking ID - safe substring
        if (holder.bookingId != null) {
            String bookingId = "N/A";
            if (booking.id != null && booking.id.length() > 0) {
                bookingId = booking.id.length() > 8 ? booking.id.substring(0, 8) : booking.id;
            }
            holder.bookingId.setText("ID: #" + bookingId);
        }

        // Set date
        if (holder.date != null) {
            holder.date.setText("Date: " + (booking.booking_date != null ? booking.booking_date : "N/A"));
        }

        // Set time slot
        if (holder.slot != null) {
            holder.slot.setText("Slot: " + (booking.time_slot != null ? booking.time_slot : "N/A"));
        }

        // Set status
        if (holder.status != null) {
            String status = booking.status != null ? booking.status : "Unknown";
            holder.status.setText("Status: " + status);
            
            // Set status color
            int statusColor;
            if (status.equalsIgnoreCase("confirmed") || status.equalsIgnoreCase("active")) {
                statusColor = ContextCompat.getColor(context, R.color.success);
            } else if (status.equalsIgnoreCase("cancelled") || status.equalsIgnoreCase("rejected")) {
                statusColor = ContextCompat.getColor(context, R.color.error);
            } else if (status.equalsIgnoreCase("pending")) {
                statusColor = ContextCompat.getColor(context, R.color.accent);
            } else {
                statusColor = ContextCompat.getColor(context, R.color.text_secondary);
            }
            holder.status.setTextColor(statusColor);
        }

        // Set price
        if (holder.price != null) {
            holder.price.setText("₹" + (int) booking.price);
        }

        // Handle action buttons (Cancel for pending, Feedback for confirmed)
        String status = booking.status != null ? booking.status.toLowerCase() : "";
        boolean isPending = status.equals("pending");
        boolean isConfirmed = status.equals("confirmed") || status.equals("paid") || 
                            (booking.payment_status != null && booking.payment_status.equalsIgnoreCase("paid"));
        
        // Show/hide action buttons layout
        if (holder.actionButtonsLayout != null) {
            holder.actionButtonsLayout.setVisibility((isPending || isConfirmed) ? View.VISIBLE : View.GONE);
        }
        
        // Cancel button - only for pending bookings
        if (holder.cancelButton != null) {
            holder.cancelButton.setVisibility(isPending ? View.VISIBLE : View.GONE);
            if (isPending) {
                holder.cancelButton.setOnClickListener(v -> cancelBooking(booking.id, position));
            }
        }
        
        // Feedback button - only for confirmed/paid bookings
        if (holder.feedbackButton != null) {
            holder.feedbackButton.setVisibility(isConfirmed ? View.VISIBLE : View.GONE);
            if (isConfirmed) {
                holder.feedbackButton.setOnClickListener(v -> {
                    FeedbackDialog dialog = new FeedbackDialog(context, booking.id, () -> {
                        // Reload bookings if needed
                        if (context instanceof com.example.groundbookingsystem.MyBookingsActivity) {
                            ((com.example.groundbookingsystem.MyBookingsActivity) context).loadAllBookings();
                        }
                    });
                    dialog.show();
                });
            }
        }
    }
    
    private void cancelBooking(String bookingId, int position) {
        if (token == null || token.isEmpty()) {
            Toast.makeText(context, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String authToken = token.startsWith("Bearer ") ? token : "Bearer " + token;
        apiService.cancelBooking(bookingId, authToken).enqueue(new Callback<CancelBookingResponse>() {
            @Override
            public void onResponse(Call<CancelBookingResponse> call, Response<CancelBookingResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    Toast.makeText(context, "Booking cancelled successfully", Toast.LENGTH_SHORT).show();
                    // Update booking status
                    if (position < bookings.size()) {
                        bookings.get(position).status = "cancelled";
                        notifyItemChanged(position);
                    }
                    // Reload bookings if needed
                    if (context instanceof com.example.groundbookingsystem.MyBookingsActivity) {
                        ((com.example.groundbookingsystem.MyBookingsActivity) context).loadAllBookings();
                    }
                } else {
                    String errorMessage = "Failed to cancel booking";
                    if (response.errorBody() != null) {
                        try {
                            android.util.Log.e("BookingAdapter", "Error: " + response.errorBody().string());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<CancelBookingResponse> call, Throwable t) {
                android.util.Log.e("BookingAdapter", "Cancel booking error: " + t.getMessage());
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookings != null ? bookings.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, bookingId, date, slot, status, price;
        MaterialButton feedbackButton, cancelButton;
        LinearLayout actionButtonsLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.bookingName);
            bookingId = itemView.findViewById(R.id.bookingId);
            date = itemView.findViewById(R.id.bookingDate);
            slot = itemView.findViewById(R.id.bookingTime);
            status = itemView.findViewById(R.id.bookingStatus);
            price = itemView.findViewById(R.id.bookingPrice);
            feedbackButton = itemView.findViewById(R.id.feedbackButton);
            cancelButton = itemView.findViewById(R.id.cancelBookingButton);
            actionButtonsLayout = itemView.findViewById(R.id.actionButtonsLayout);
        }
    }
}
