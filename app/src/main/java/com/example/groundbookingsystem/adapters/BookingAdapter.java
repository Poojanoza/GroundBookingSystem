package com.example.groundbookingsystem.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groundbookingsystem.R;
import com.example.groundbookingsystem.models.Booking;
import com.example.groundbookingsystem.models.Ground;

import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {

    private List<Booking> bookings;
    private Context context;

    public BookingAdapter(List<Booking> bookings, Context context) {
        this.bookings = bookings;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookings.get(position);

        Ground ground = booking.grounds != null ? booking.grounds : booking.ground;
        holder.name.setText(ground != null ? ground.name : "Ground");
        holder.date.setText("Date: " + booking.booking_date);
        holder.slot.setText("Slot: " + booking.time_slot);
        holder.status.setText("Status: " + booking.status);
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, date, slot, status;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.bookingName);
            date = itemView.findViewById(R.id.bookingDate);
            slot = itemView.findViewById(R.id.bookingTime);
            status = itemView.findViewById(R.id.bookingStatus);
        }
    }
}
