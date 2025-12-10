package com.example.groundbookingsystem.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groundbookingsystem.R;
import com.example.groundbookingsystem.models.AdminBooking;

import java.util.List;

public class AdminBookingAdapter extends RecyclerView.Adapter<AdminBookingAdapter.ViewHolder> {

    private List<AdminBooking> bookings;
    private Context context;
    private OnActionListener listener;

    public interface OnActionListener {
        void onApprove(AdminBooking booking);
        void onReject(AdminBooking booking);
    }

    public AdminBookingAdapter(Context context, List<AdminBooking> bookings, OnActionListener listener) {
        this.context = context;
        this.bookings = bookings;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminBooking booking = bookings.get(position);

        // Bind data
        String groundName = booking.grounds != null ? booking.grounds.name : (booking.ground_id != null ? booking.ground_id : "N/A");
        holder.groundView.setText(groundName);
        holder.bookingIdView.setText(booking.id != null ? "ID: #" + (booking.id.length() > 8 ? booking.id.substring(0, 8) : booking.id) : "ID: N/A");

        holder.groundLocationView.setText("Location: " + (booking.grounds != null && booking.grounds.location != null ? booking.grounds.location : "N/A"));
        holder.groundTypeView.setText("Type: " + (booking.grounds != null && booking.grounds.type != null ? booking.grounds.type : "N/A"));
        // Format date
        String dateText = booking.booking_date != null ? booking.booking_date : "N/A";
        holder.dateView.setText(dateText);
        
        // Format time slot
        String slotText = booking.time_slot != null ? booking.time_slot : "N/A";
        holder.slotView.setText(slotText);
        
        // Customer information
        if (booking.users != null) {
            holder.customerName.setText(booking.users.name != null ? booking.users.name : "Unknown User");
            String phoneText = booking.users.phone != null && !booking.users.phone.isEmpty() 
                ? "📞 " + booking.users.phone : "📞 Not provided";
            holder.customerPhone.setText(phoneText);
            String emailText = booking.users.email != null && !booking.users.email.isEmpty() 
                ? "✉️ " + booking.users.email : "✉️ Not provided";
            holder.customerEmail.setText(emailText);
        } else {
            holder.customerName.setText("Unknown User");
            holder.customerPhone.setText("📞 Not provided");
            holder.customerEmail.setText("✉️ Not provided");
        }

        holder.priceView.setText("₹" + booking.price);

        // Status color and badge background
        String status = booking.status != null ? booking.status.toLowerCase() : "confirmed";
        if ("confirmed".equalsIgnoreCase(status) || "approved".equalsIgnoreCase(status)) {
            holder.statusView.setText("Confirmed");
            holder.statusView.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.success));
            holder.statusView.setTextColor(ContextCompat.getColor(context, android.R.color.white));
            holder.approveBtn.setVisibility(View.GONE);
            holder.rejectBtn.setVisibility(View.VISIBLE);
        } else if ("cancelled".equalsIgnoreCase(status) || "rejected".equalsIgnoreCase(status)) {
            holder.statusView.setText("Cancelled");
            holder.statusView.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.error));
            holder.statusView.setTextColor(ContextCompat.getColor(context, android.R.color.white));
            holder.approveBtn.setVisibility(View.GONE);
            holder.rejectBtn.setVisibility(View.GONE);
        } else if ("pending".equalsIgnoreCase(status)) {
            holder.statusView.setText("Pending");
            holder.statusView.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.accent));
            holder.statusView.setTextColor(ContextCompat.getColor(context, android.R.color.white));
            holder.approveBtn.setVisibility(View.VISIBLE);
            holder.rejectBtn.setVisibility(View.VISIBLE);
        } else {
            holder.statusView.setText(status.substring(0, 1).toUpperCase() + status.substring(1));
            holder.statusView.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.text_secondary));
            holder.statusView.setTextColor(ContextCompat.getColor(context, android.R.color.white));
            holder.approveBtn.setVisibility(View.GONE);
            holder.rejectBtn.setVisibility(View.GONE);
        }

        holder.approveBtn.setOnClickListener(v -> {
            if (listener != null) listener.onApprove(booking);
        });

        holder.rejectBtn.setOnClickListener(v -> {
            if (listener != null) listener.onReject(booking);
        });
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView groundView, bookingIdView, groundLocationView, groundTypeView, dateView, slotView, statusView, customerName, customerEmail, customerPhone, priceView;
        Button approveBtn, rejectBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Use IDs from item_admin_booking.xml
            groundView = itemView.findViewById(R.id.groundNameAdmin);
            bookingIdView = itemView.findViewById(R.id.bookingIdAdmin);
            groundLocationView = itemView.findViewById(R.id.groundLocationAdmin);
            groundTypeView = itemView.findViewById(R.id.groundTypeAdmin);
            dateView = itemView.findViewById(R.id.dateAdmin);
            slotView = itemView.findViewById(R.id.timeSlotAdmin);
            statusView = itemView.findViewById(R.id.statusAdmin);
            
            customerName = itemView.findViewById(R.id.customerNameAdmin);
            customerEmail = itemView.findViewById(R.id.customerEmailAdmin);
            customerPhone = itemView.findViewById(R.id.customerPhoneAdmin);
            priceView = itemView.findViewById(R.id.priceAdmin);
            
            approveBtn = itemView.findViewById(R.id.approveBtn);
            rejectBtn = itemView.findViewById(R.id.rejectBtn);
        }
    }
}
