package com.example.groundbookingsystem;

import android.os.Bundle;
import com.example.groundbookingsystem.R;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.groundbookingsystem.models.Booking;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class GroundDetailActivity extends AppCompatActivity {

    private ImageView groundImageView;
    private TextView groundNameTextView, groundLocationTextView, groundTypeTextView;
    private TextView groundDescriptionTextView, groundPriceTextView, selectedTimeSlot;
    private CalendarView calendarView;
    private GridLayout timeSlotsGrid;
    private Button bookButton;

    private String groundId, groundName, groundLocation, groundDescription, groundImageUrl, groundType;
    private double groundPrice;
    private String selectedDate;
    private String selectedSlot = "";
    private List<String> bookedSlots = new ArrayList<>();

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Time slots
    private String[] timeSlots = {
            "06:00 AM", "07:00 AM", "08:00 AM", "09:00 AM", "10:00 AM", "11:00 AM",
            "12:00 PM", "01:00 PM", "02:00 PM", "03:00 PM", "04:00 PM", "05:00 PM",
            "06:00 PM", "07:00 PM", "08:00 PM", "09:00 PM", "10:00 PM"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ground_detail);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Get ground data from intent
        groundId = getIntent().getStringExtra("groundId");
        groundName = getIntent().getStringExtra("groundName");
        groundLocation = getIntent().getStringExtra("groundLocation");
        groundDescription = getIntent().getStringExtra("groundDescription");
        groundImageUrl = getIntent().getStringExtra("groundImageUrl");
        groundPrice = getIntent().getDoubleExtra("groundPrice", 0);
        groundType = getIntent().getStringExtra("groundType");

        // Initialize views
        groundImageView = findViewById(R.id.groundImageView);
        groundNameTextView = findViewById(R.id.groundNameTextView);
        groundLocationTextView = findViewById(R.id.groundLocationTextView);
        groundTypeTextView = findViewById(R.id.groundTypeTextView);
        groundDescriptionTextView = findViewById(R.id.groundDescriptionTextView);
        groundPriceTextView = findViewById(R.id.groundPriceTextView);
        calendarView = findViewById(R.id.calendarView);
        timeSlotsGrid = findViewById(R.id.timeSlotsGrid);
        bookButton = findViewById(R.id.bookButton);
        selectedTimeSlot = findViewById(R.id.selectedTimeSlot);

        // Set ground details
        groundNameTextView.setText(groundName);
        groundLocationTextView.setText(groundLocation);
        groundTypeTextView.setText("Type: " + groundType);
        groundDescriptionTextView.setText(groundDescription);
        groundPriceTextView.setText("₹" + groundPrice + "/hour");

        // Load image
        if (groundImageUrl != null && !groundImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(groundImageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(groundImageView);
        }

        // Set default selected date to today
        Calendar today = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        selectedDate = dateFormat.format(today.getTime());

        // Set minimum date to today
        calendarView.setMinDate(today.getTimeInMillis());

        // Calendar date selection
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.set(year, month, dayOfMonth);
            selectedDate = dateFormat.format(selectedCalendar.getTime());
            selectedSlot = "";
            selectedTimeSlot.setText("No slot selected");
            loadBookedSlots();
        });

        // Load initial time slots
        loadBookedSlots();

        // Book button click
        bookButton.setOnClickListener(v -> confirmBooking());
    }

    private void loadBookedSlots() {
        bookedSlots.clear();

        mDatabase.child("bookings")
                .orderByChild("groundId")
                .equalTo(groundId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Booking booking = dataSnapshot.getValue(Booking.class);
                            if (booking != null && booking.getDate().equals(selectedDate)
                                    && !booking.getStatus().equals("cancelled")) {
                                bookedSlots.add(booking.getTimeSlot());
                            }
                        }
                        displayTimeSlots();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(GroundDetailActivity.this,
                                "Failed to load slots", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayTimeSlots() {
        timeSlotsGrid.removeAllViews();

        for (String slot : timeSlots) {
            Button slotButton = new Button(this);
            slotButton.setText(slot);
            slotButton.setTextSize(12);
            slotButton.setPadding(8, 16, 8, 16);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(8, 8, 8, 8);
            slotButton.setLayoutParams(params);

            // Check if slot is booked
            if (bookedSlots.contains(slot)) {
                slotButton.setEnabled(false);
                slotButton.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                slotButton.setTextColor(getResources().getColor(android.R.color.white));
            } else {
                slotButton.setOnClickListener(v -> {
                    selectedSlot = slot;
                    selectedTimeSlot.setText("Selected: " + slot);
                    selectedTimeSlot.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    Toast.makeText(GroundDetailActivity.this,
                            "Selected: " + slot, Toast.LENGTH_SHORT).show();
                });
            }

            timeSlotsGrid.addView(slotButton);
        }
    }

    private void confirmBooking() {
        if (selectedSlot.isEmpty()) {
            Toast.makeText(this, "Please select a time slot", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Confirm Booking")
                .setMessage("Ground: " + groundName + "\n" +
                        "Date: " + selectedDate + "\n" +
                        "Time: " + selectedSlot + "\n" +
                        "Price: ₹" + groundPrice + "\n\n" +
                        "Confirm this booking?")
                .setPositiveButton("Confirm", (dialog, which) -> createBooking(currentUser))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void createBooking(FirebaseUser user) {
        String bookingId = mDatabase.child("bookings").push().getKey();

        if (bookingId == null) {
            Toast.makeText(this, "Booking failed", Toast.LENGTH_SHORT).show();
            return;
        }

        Booking booking = new Booking(
                bookingId,
                user.getUid(),
                user.getDisplayName() != null ? user.getDisplayName() : "User",
                user.getEmail(),
                groundId,
                groundName,
                selectedDate,
                selectedSlot,
                groundPrice,
                "confirmed",
                System.currentTimeMillis()
        );

        mDatabase.child("bookings").child(bookingId).setValue(booking)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Booking confirmed!", Toast.LENGTH_LONG).show();

                        // Show success dialog
                        new AlertDialog.Builder(this)
                                .setTitle("Booking Successful!")
                                .setMessage("Your booking has been confirmed.\n\n" +
                                        "Booking ID: " + bookingId + "\n" +
                                        "Date: " + selectedDate + "\n" +
                                        "Time: " + selectedSlot)
                                .setPositiveButton("OK", (dialog, which) -> finish())
                                .setCancelable(false)
                                .show();
                    } else {
                        Toast.makeText(this, "Booking failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
