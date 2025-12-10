package com.example.groundbookingsystem.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.groundbookingsystem.R;
import com.example.groundbookingsystem.api.ApiClient;
import com.example.groundbookingsystem.api.ApiService;
import com.example.groundbookingsystem.models.Booking;
import com.example.groundbookingsystem.models.CancelBookingResponse;
import com.example.groundbookingsystem.models.Ground;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class BookingHistoryAdapter extends RecyclerView.Adapter<BookingHistoryAdapter.ViewHolder> {

    private Context context;
    private List<Booking> bookingList;
    private ApiService apiService;
    private String token;

    public BookingHistoryAdapter(Context context, List<Booking> bookingList) {
        this.context = context;
        this.bookingList = bookingList;
        this.apiService = ApiClient.getClient().create(ApiService.class);
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        this.token = prefs.getString("token", "");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        Ground ground = booking.grounds != null ? booking.grounds : booking.ground;
        holder.groundNameView.setText("Ground: " + (ground != null ? ground.name : "N/A"));
        holder.dateView.setText("Date: " + booking.booking_date);
        holder.timeSlotView.setText("Time: " + booking.time_slot);
        holder.priceView.setText("Price: ₹" + booking.price);
        holder.statusView.setText("Status: " + booking.status);

        // Set status color
        if ("confirmed".equalsIgnoreCase(booking.status)) {
            holder.statusView.setTextColor(context.getResources().getColor(R.color.green));
        } else if ("cancelled".equalsIgnoreCase(booking.status)) {
            holder.statusView.setTextColor(context.getResources().getColor(R.color.red));
        } else {
            holder.statusView.setTextColor(context.getResources().getColor(R.color.orange));
        }

        // Show cancel button only for confirmed bookings
        if ("confirmed".equalsIgnoreCase(booking.status)) {
            holder.cancelBtn.setVisibility(View.VISIBLE);
            holder.cancelBtn.setOnClickListener(v -> cancelBooking(booking.id, holder.getAdapterPosition()));
        } else {
            holder.cancelBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    private void cancelBooking(String bookingId, int position) {
        apiService.cancelBooking(bookingId, "Bearer " + token).enqueue(new Callback<CancelBookingResponse>() {
            @Override
            public void onResponse(Call<CancelBookingResponse> call, Response<CancelBookingResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    Toast.makeText(context, "Booking cancelled", Toast.LENGTH_SHORT).show();
                    if (position < bookingList.size()) {
                        bookingList.get(position).status = "cancelled";
                        notifyItemChanged(position);
                    }
                } else {
                    Toast.makeText(context, "Failed to cancel", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CancelBookingResponse> call, Throwable t) {
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView groundNameView, dateView, timeSlotView, priceView, statusView;
        Button cancelBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            groundNameView = itemView.findViewById(R.id.groundNameHistory);
            dateView = itemView.findViewById(R.id.dateHistory);
            timeSlotView = itemView.findViewById(R.id.timeSlotHistory);
            priceView = itemView.findViewById(R.id.priceHistory);
            statusView = itemView.findViewById(R.id.statusHistory);
            cancelBtn = itemView.findViewById(R.id.cancelBookingBtn);
        }
    }
}
