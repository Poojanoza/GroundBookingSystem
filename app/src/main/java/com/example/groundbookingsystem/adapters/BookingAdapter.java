package com.example.groundbookingsystem.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groundbookingsystem.R;
import com.example.groundbookingsystem.models.Booking;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private Context context;
    private List<Booking> bookingList;

    public BookingAdapter(Context context, List<Booking> bookingList) {
        this.context = context;
        this.bookingList = bookingList;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        holder.bookingIdTextView.setText("Booking ID: #" + booking.getId().substring(0, 8));
        holder.groundNameTextView.setText(booking.getGroundName());
        holder.dateTextView.setText("Date: " + booking.getDate());
        holder.timeSlotTextView.setText("Time: " + booking.getTimeSlot());
        holder.priceTextView.setText("₹" + booking.getTotalPrice());
        holder.statusTextView.setText(booking.getStatus().toUpperCase());

        // Set status color
        if (booking.getStatus().equals("confirmed")) {
            holder.statusTextView.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else if (booking.getStatus().equals("cancelled")) {
            holder.statusTextView.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            holder.cancelButton.setVisibility(View.GONE);
        } else {
            holder.statusTextView.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
        }

        // Cancel button
        holder.cancelButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Cancel Booking")
                    .setMessage("Are you sure you want to cancel this booking?")
                    .setPositiveButton("Yes", (dialog, which) -> cancelBooking(booking, position))
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private void cancelBooking(Booking booking, int position) {
        DatabaseReference bookingRef = FirebaseDatabase.getInstance()
                .getReference("bookings")
                .child(booking.getId());

        bookingRef.child("status").setValue("cancelled")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        booking.setStatus("cancelled");
                        notifyItemChanged(position);
                        Toast.makeText(context, "Booking cancelled", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Failed to cancel booking", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView bookingIdTextView, groundNameTextView, dateTextView, timeSlotTextView;
        TextView priceTextView, statusTextView;
        Button cancelButton;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            bookingIdTextView = itemView.findViewById(R.id.bookingIdTextView);
            groundNameTextView = itemView.findViewById(R.id.groundNameTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            timeSlotTextView = itemView.findViewById(R.id.timeSlotTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            cancelButton = itemView.findViewById(R.id.cancelButton);
        }
    }
}
