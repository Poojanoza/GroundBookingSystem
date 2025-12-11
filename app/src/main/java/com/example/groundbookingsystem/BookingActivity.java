package com.example.groundbookingsystem;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.groundbookingsystem.api.ApiClient;
import com.example.groundbookingsystem.api.ApiService;
import com.example.groundbookingsystem.models.BookingRequest;
import com.example.groundbookingsystem.models.BookingResponse;
import com.example.groundbookingsystem.models.Ground;
import com.example.groundbookingsystem.models.SlotsResponse;
import com.google.gson.Gson;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingActivity extends AppCompatActivity {

    private Ground ground;
    private String selectedDate = "";
    private String selectedSlot = "";
    private ProgressBar progressBar;
    private Button bookBtn;
    private TextView selectedSlotView;
    private GridLayout slotsContainer;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Confirm Booking");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        ground = (Ground) getIntent().getSerializableExtra("ground");
        apiService = ApiClient.getClient().create(ApiService.class);

        TextView groundName = findViewById(R.id.groundNameBooking);
        TextView groundPrice = findViewById(R.id.groundPriceBooking);
        TextView dateView = findViewById(R.id.selectedDateView);
        slotsContainer = findViewById(R.id.slotsGrid);
        bookBtn = findViewById(R.id.confirmBookingBtn);
        progressBar = findViewById(R.id.progressBar);
        selectedSlotView = findViewById(R.id.selectedSlotView);
        Button datePickerBtn = findViewById(R.id.selectDateBtn);

        if (ground != null) {
            groundName.setText(ground.name);
            groundPrice.setText("₹" + ground.price + " per hour");
        }

        datePickerBtn.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                selectedDate = year + "-" + String.format("%02d", month + 1) + "-" + String.format("%02d", dayOfMonth);
                dateView.setText("Selected: " + selectedDate);
                dateView.setVisibility(View.VISIBLE);
                loadAvailableSlots(selectedDate);
            }, 2024, 0, 1);
            dialog.show();
        });

        // Book button
        bookBtn.setOnClickListener(v -> {
            if (selectedDate.isEmpty() || selectedSlot.isEmpty()) {
                Toast.makeText(this, "Please select date and slot", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            bookBtn.setEnabled(false);

            SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
            String token = prefs.getString("token", "");

            BookingRequest bookingRequest = new BookingRequest(ground.id, selectedDate, selectedSlot, ground.price);

            apiService.createBooking("Bearer " + token, bookingRequest)
                    .enqueue(new Callback<BookingResponse>() {
                        @Override
                        public void onResponse(Call<BookingResponse> call, Response<BookingResponse> response) {
                            progressBar.setVisibility(View.GONE);
                            bookBtn.setEnabled(true);

                            if (response.isSuccessful() && response.body() != null) {
                                if (response.body().success || response.body().data != null) {
                                    // Navigate to payment screen
                                    Intent paymentIntent = new Intent(BookingActivity.this, PaymentActivity.class);
                                    paymentIntent.putExtra("booking", response.body().data);
                                    paymentIntent.putExtra("ground", ground);
                                    startActivityForResult(paymentIntent, 1001);
                                } else {
                                     Toast.makeText(BookingActivity.this,
                                            "Booking failed: " + response.body().message,
                                            Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                String errorMessage = "Booking failed";
                                if (response.errorBody() != null) {
                                    try {
                                        BookingResponse errorResponse = new Gson().fromJson(response.errorBody().charStream(), BookingResponse.class);
                                        if (errorResponse != null && errorResponse.message != null) {
                                            errorMessage = errorResponse.message;
                                        } else {
                                             errorMessage = "Server Error: " + response.code();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        errorMessage = "Server Error: " + response.code();
                                    }
                                }
                                Toast.makeText(BookingActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<BookingResponse> call, Throwable t) {
                            progressBar.setVisibility(View.GONE);
                            bookBtn.setEnabled(true);
                            Toast.makeText(BookingActivity.this,
                                    "Error: " + t.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("payment_success", false)) {
                Toast.makeText(this, "Payment completed successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadAvailableSlots(String date) {
        progressBar.setVisibility(View.VISIBLE);
        slotsContainer.removeAllViews();
        selectedSlot = "";
        if (selectedSlotView != null) selectedSlotView.setText("");
        bookBtn.setEnabled(false);

        apiService.getAvailableSlots(ground.id, date).enqueue(new Callback<SlotsResponse>() {
            @Override
            public void onResponse(Call<SlotsResponse> call, Response<SlotsResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    populateSlots(response.body().availableSlots);
                } else {
                    Toast.makeText(BookingActivity.this, "Failed to load slots", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SlotsResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(BookingActivity.this, "Error loading slots", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateSlots(List<String> slots) {
        if (slots == null || slots.isEmpty()) {
            Toast.makeText(this, "No slots available for this date", Toast.LENGTH_SHORT).show();
            return;
        }

        for (String slot : slots) {
            Button slotBtn = new Button(this);
            slotBtn.setText(slot);
            slotBtn.setTextSize(12);
            slotBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.green));

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = GridLayout.LayoutParams.WRAP_CONTENT;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.setMargins(8, 8, 8, 8);
            slotBtn.setLayoutParams(params);

            slotBtn.setOnClickListener(v -> {
                selectedSlot = slot;
                bookBtn.setEnabled(true);
                if (selectedSlotView != null) {
                    selectedSlotView.setText("Selected: " + slot);
                }
                Toast.makeText(BookingActivity.this, "Slot selected: " + slot, Toast.LENGTH_SHORT).show();
            });

            slotsContainer.addView(slotBtn);
        }
    }
}
