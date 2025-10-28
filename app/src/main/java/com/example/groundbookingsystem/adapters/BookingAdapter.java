package com.example.groundbookingsystem.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groundbookingsystem.R;
import com.example.groundbookingsystem.models.Booking;
import com.example.groundbookingsystem.models.Ground;
import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {
    private List<Booking> bookings;
    private Context context;

    public BookingAdapter(Context context, List<Booking> bookings) {
        this.context = context;
        this.bookings = bookings;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        Ground ground = booking.ground;
        holder.name.setText(ground != null ? ground.name : booking.ground_id);
        holder.date.setText("Date: " + booking.booking_date);
        holder.time.setText("Slot: " + booking.time_slot);
        holder.status.setText("Status: " + booking.status);
        holder.price.setText("₹" + booking.price);
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, date, time, status, price;
        ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.bookingName);
            date = view.findViewById(R.id.bookingDate);
            time = view.findViewById(R.id.bookingTime);
            status = view.findViewById(R.id.bookingStatus);
            price = view.findViewById(R.id.bookingPrice);
        }
    }
}
