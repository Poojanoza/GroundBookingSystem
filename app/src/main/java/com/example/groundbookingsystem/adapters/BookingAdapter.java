package com.example.groundbookingsystem.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groundbookingsystem.R;
import com.example.groundbookingsystem.models.Booking;
import com.example.groundbookingsystem.models.Ground;

import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {

    private List<Booking> bookings;
    private Context context;

    public BookingAdapter(List<Booking> bookings, Context context) {
        this.bookings = bookings != null ? bookings : new java.util.ArrayList<>();
        this.context = context;
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
    }

    @Override
    public int getItemCount() {
        return bookings != null ? bookings.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, bookingId, date, slot, status, price;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.bookingName);
            bookingId = itemView.findViewById(R.id.bookingId);
            date = itemView.findViewById(R.id.bookingDate);
            slot = itemView.findViewById(R.id.bookingTime);
            status = itemView.findViewById(R.id.bookingStatus);
            price = itemView.findViewById(R.id.bookingPrice);
        }
    }
}
