package com.example.groundbookingsystem;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groundbookingsystem.adapters.AdminBookingAdapter;
import com.example.groundbookingsystem.api.ApiClient;
import com.example.groundbookingsystem.api.ApiService;
import com.example.groundbookingsystem.models.AdminBooking;
import com.example.groundbookingsystem.models.AdminBookingsResponse;
import com.example.groundbookingsystem.utils.ToastUtil;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminAllBookingsActivity extends AppCompatActivity implements AdminBookingAdapter.OnActionListener {

    private RecyclerView recyclerView;
    private AdminBookingAdapter adapter;
    private List<AdminBooking> bookingList = new ArrayList<>();
    private ProgressBar progressBar;
    private View emptyBookingsView;
    private ApiService apiService;
    private String token;
    private MaterialButton filterBtn;
    private String startDate = "";
    private String endDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_all_bookings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("All Bookings");
        }

        initViews();

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        token = prefs.getString("token", "");

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(false);
        adapter = new AdminBookingAdapter(this, bookingList, this);
        recyclerView.setAdapter(adapter);

        apiService = ApiClient.getClient().create(ApiService.class);
        
        // Setup filter button
        filterBtn = findViewById(R.id.filterBtn);
        if (filterBtn != null) {
            filterBtn.setOnClickListener(v -> showFilterDialog());
        }
        
        loadAllBookings();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        recyclerView = findViewById(R.id.bookingsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyBookingsView = findViewById(R.id.emptyBookingsView);
    }

    private void loadAllBookings() {
        progressBar.setVisibility(View.VISIBLE);
        emptyBookingsView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        apiService.getAdminBookings("Bearer " + token).enqueue(new Callback<AdminBookingsResponse>() {
            @Override
            public void onResponse(Call<AdminBookingsResponse> call, Response<AdminBookingsResponse> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().success) {
                        if (response.body().data != null) {
                            bookingList.clear();
                            bookingList.addAll(response.body().data);
                            adapter.notifyDataSetChanged();
                            
                            if (bookingList.isEmpty()) {
                                emptyBookingsView.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            } else {
                                emptyBookingsView.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                                ToastUtil.showSuccess(AdminAllBookingsActivity.this, "Loaded " + bookingList.size() + " bookings");
                            }
                        } else {
                            emptyBookingsView.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        }
                    } else {
                        ToastUtil.showError(AdminAllBookingsActivity.this, "Failed to load bookings");
                        emptyBookingsView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                } else {
                    ToastUtil.showError(AdminAllBookingsActivity.this, "Error: " + response.code());
                    emptyBookingsView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<AdminBookingsResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                ToastUtil.showError(AdminAllBookingsActivity.this, "Error: " + t.getMessage());
                emptyBookingsView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onApprove(AdminBooking booking) {
        ToastUtil.showInfo(this, "Approve functionality not implemented on server");
    }

    @Override
    public void onReject(AdminBooking booking) {
        progressBar.setVisibility(View.VISIBLE);
        apiService.cancelBooking(booking.id, "Bearer " + token).enqueue(new Callback<com.example.groundbookingsystem.models.CancelBookingResponse>() {
            @Override
            public void onResponse(Call<com.example.groundbookingsystem.models.CancelBookingResponse> call, Response<com.example.groundbookingsystem.models.CancelBookingResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    ToastUtil.showSuccess(AdminAllBookingsActivity.this, "Booking Rejected/Cancelled");
                    loadAllBookings();
                } else {
                    ToastUtil.showError(AdminAllBookingsActivity.this, "Failed to reject booking");
                }
            }

            @Override
            public void onFailure(Call<com.example.groundbookingsystem.models.CancelBookingResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                ToastUtil.showError(AdminAllBookingsActivity.this, "Error: " + t.getMessage());
            }
        });
    }

    private void showFilterDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_filter_bookings, null);
        
        MaterialButton startDateBtn = dialogView.findViewById(R.id.startDateBtn);
        MaterialButton endDateBtn = dialogView.findViewById(R.id.endDateBtn);
        MaterialButton applyFilterBtn = dialogView.findViewById(R.id.applyFilterBtn);
        MaterialButton clearFilterBtn = dialogView.findViewById(R.id.clearFilterBtn);
        
        if (!startDate.isEmpty()) {
            startDateBtn.setText("From: " + startDate);
        }
        if (!endDate.isEmpty()) {
            endDateBtn.setText("To: " + endDate);
        }
        
        startDateBtn.setOnClickListener(v -> showDatePickerForStart(startDateBtn));
        endDateBtn.setOnClickListener(v -> showDatePickerForEnd(endDateBtn));
        
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Filter Bookings by Date")
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .create();
        
        applyFilterBtn.setOnClickListener(v -> {
            if (startDate.isEmpty() || endDate.isEmpty()) {
                ToastUtil.showWarning(this, "Please select both dates");
                return;
            }
            filterBookings();
            dialog.dismiss();
        });
        
        clearFilterBtn.setOnClickListener(v -> {
            startDate = "";
            endDate = "";
            startDateBtn.setText("Select Start Date");
            endDateBtn.setText("Select End Date");
            loadAllBookings();
            dialog.dismiss();
        });
        
        dialog.show();
    }
    
    private void showDatePickerForStart(MaterialButton button) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            startDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            button.setText("From: " + startDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }
    
    private void showDatePickerForEnd(MaterialButton button) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            endDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            button.setText("To: " + endDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }
    
    private void filterBookings() {
        progressBar.setVisibility(View.VISIBLE);
        emptyBookingsView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        apiService.getBookingsByDateRange(startDate, endDate, "Bearer " + token)
                .enqueue(new Callback<AdminBookingsResponse>() {
                    @Override
                    public void onResponse(Call<AdminBookingsResponse> call, Response<AdminBookingsResponse> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null && response.body().success) {
                            bookingList.clear();
                            bookingList.addAll(response.body().data);
                            adapter.notifyDataSetChanged();
                            
                            if (bookingList.isEmpty()) {
                                emptyBookingsView.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                                ToastUtil.showInfo(AdminAllBookingsActivity.this, "No bookings found for selected dates");
                            } else {
                                emptyBookingsView.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                                ToastUtil.showSuccess(AdminAllBookingsActivity.this, "Found " + bookingList.size() + " bookings");
                            }
                        } else {
                            ToastUtil.showError(AdminAllBookingsActivity.this, "Failed to filter bookings");
                        }
                    }

                    @Override
                    public void onFailure(Call<AdminBookingsResponse> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        ToastUtil.showError(AdminAllBookingsActivity.this, "Error: " + t.getMessage());
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllBookings();
    }
}

