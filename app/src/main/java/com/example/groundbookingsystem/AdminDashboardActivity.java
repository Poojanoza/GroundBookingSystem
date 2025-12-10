package com.example.groundbookingsystem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.groundbookingsystem.utils.ToastUtil;

import com.example.groundbookingsystem.adapters.AdminBookingAdapter;
import com.example.groundbookingsystem.adapters.AdminGroundAdapter;
import com.example.groundbookingsystem.api.ApiClient;
import com.example.groundbookingsystem.api.ApiService;
import com.example.groundbookingsystem.models.AdminBooking;
import com.example.groundbookingsystem.models.AdminBookingsResponse;
import com.example.groundbookingsystem.models.AdminStatsResponse;
import com.example.groundbookingsystem.models.CancelBookingResponse;
import com.example.groundbookingsystem.models.Ground;
import com.example.groundbookingsystem.models.GroundsResponse;
import com.example.groundbookingsystem.models.MessageResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdminDashboardActivity extends AppCompatActivity implements AdminBookingAdapter.OnActionListener, AdminGroundAdapter.OnGroundActionListener {

    private RecyclerView recyclerView;
    private AdminBookingAdapter bookingAdapter;
    private AdminGroundAdapter groundAdapter;
    private List<AdminBooking> bookingList = new ArrayList<>();
    private List<Ground> groundList = new ArrayList<>();
    private ProgressBar progressBar;
    private Button filterBtn, startDateBtn, endDateBtn, manageGroundsBtn;
    private TextView totalBookingsView, totalRevenueView, activeBookingsView, cancelledBookingsView, manageBookingsTitle;
    private TabLayout adminTabs;
    private ApiService apiService;
    private String token;
    private View emptyBookingsView;
    
    private String startDate = "";
    private String endDate = "";
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private boolean isShowingBookings = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        initViews();
        setupToolbar();

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        token = prefs.getString("token", "");

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(false);
        bookingAdapter = new AdminBookingAdapter(this, bookingList, this);
        groundAdapter = new AdminGroundAdapter(this, groundList, this);
        recyclerView.setAdapter(bookingAdapter);
        
        // Initial visibility
        if (emptyBookingsView != null) {
            emptyBookingsView.setVisibility(View.GONE);
        }
        recyclerView.setVisibility(View.VISIBLE);

        apiService = ApiClient.getClient().create(ApiService.class);

        // Setup tabs
        adminTabs = findViewById(R.id.adminTabs);
        if (adminTabs != null) {
            adminTabs.addTab(adminTabs.newTab().setText("Booked Grounds"));
            adminTabs.addTab(adminTabs.newTab().setText("My Grounds"));
            
            adminTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if (tab.getPosition() == 0) {
                        // Booked Grounds tab
                        isShowingBookings = true;
                        recyclerView.setAdapter(bookingAdapter);
                        manageBookingsTitle.setVisibility(View.VISIBLE);
                        findViewById(R.id.filterCardView).setVisibility(View.VISIBLE);
                        loadAllBookings();
                    } else {
                        // My Grounds tab
                        isShowingBookings = false;
                        recyclerView.setAdapter(groundAdapter);
                        manageBookingsTitle.setVisibility(View.GONE);
                        findViewById(R.id.filterCardView).setVisibility(View.GONE);
                        loadAllGrounds();
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}

                @Override
                public void onTabReselected(TabLayout.Tab tab) {}
            });
        }

        startDateBtn.setOnClickListener(v -> showDatePickerForStart());
        endDateBtn.setOnClickListener(v -> showDatePickerForEnd());
        filterBtn.setOnClickListener(v -> filterBookings());
        
        manageGroundsBtn = findViewById(R.id.manageGroundsBtn);
        if (manageGroundsBtn != null) {
            manageGroundsBtn.setOnClickListener(v -> {
                startActivity(new Intent(AdminDashboardActivity.this, AdminGroundsManagementActivity.class));
            });
        }
        
        // FAB for creating ground
        View fab = findViewById(R.id.addGroundFab);
        if (fab != null) {
            fab.setOnClickListener(v -> {
                startActivity(new Intent(AdminDashboardActivity.this, AdminCreateGroundActivity.class));
            });
        }

        loadStatistics();
        loadAllBookings();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.bookingsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        filterBtn = findViewById(R.id.filterBtn);
        startDateBtn = findViewById(R.id.startDateBtn);
        endDateBtn = findViewById(R.id.endDateBtn);
        manageBookingsTitle = findViewById(R.id.manageBookingsTitle);
        emptyBookingsView = findViewById(R.id.emptyBookingsView);
        
        totalBookingsView = findViewById(R.id.totalBookingsView);
        totalRevenueView = findViewById(R.id.totalRevenueView);
        activeBookingsView = findViewById(R.id.activeBookingsView);
        cancelledBookingsView = findViewById(R.id.cancelledBookingsView);
    }
    
    private void loadAllGrounds() {
        progressBar.setVisibility(View.VISIBLE);

        apiService.getAdminGrounds("Bearer " + token).enqueue(new Callback<GroundsResponse>() {
            @Override
            public void onResponse(Call<GroundsResponse> call, Response<GroundsResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    groundList.clear();
                    groundList.addAll(response.body().data);
                    groundAdapter.notifyDataSetChanged();
                } else {
                    ToastUtil.showError(AdminDashboardActivity.this, "Failed to load grounds");
                }
            }

            @Override
            public void onFailure(Call<GroundsResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                ToastUtil.showError(AdminDashboardActivity.this, "Error: " + t.getMessage());
            }
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Admin Dashboard");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.createGroundMenu) {
            startActivity(new Intent(AdminDashboardActivity.this, AdminCreateGroundActivity.class));
            return true;
        } else if (item.getItemId() == R.id.logoutMenu) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        prefs.edit().clear().apply();
        startActivity(new Intent(AdminDashboardActivity.this, LoginActivity.class));
        finish();
    }

    private void loadStatistics() {
        apiService.getAdminStatistics("Bearer " + token).enqueue(new Callback<AdminStatsResponse>() {
            @Override
            public void onResponse(Call<AdminStatsResponse> call, Response<AdminStatsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    com.example.groundbookingsystem.models.AdminStats stats = response.body().data;
                    if (stats != null) {
                        totalBookingsView.setText(String.valueOf(stats.totalBookings));
                        totalRevenueView.setText("₹" + stats.totalRevenue);
                        activeBookingsView.setText(String.valueOf(stats.activeBookingsCount));
                        cancelledBookingsView.setText(String.valueOf(stats.cancelledBookingsCount));
                    }
                }
            }

            @Override
            public void onFailure(Call<AdminStatsResponse> call, Throwable t) {
                // Ignore or log
            }
        });
    }

    private void loadAllBookings() {
        progressBar.setVisibility(View.VISIBLE);

        apiService.getAdminBookings("Bearer " + token).enqueue(new Callback<AdminBookingsResponse>() {
            @Override
            public void onResponse(Call<AdminBookingsResponse> call, Response<AdminBookingsResponse> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().success) {
                        if (response.body().data != null) {
                            bookingList.clear();
                            bookingList.addAll(response.body().data);
                            bookingAdapter.notifyDataSetChanged();
                            
                            // Show/hide empty state
                            if (bookingList.isEmpty()) {
                                emptyBookingsView.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                                ToastUtil.showInfo(AdminDashboardActivity.this, "No bookings found");
                            } else {
                                emptyBookingsView.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                                ToastUtil.showSuccess(AdminDashboardActivity.this, "Loaded " + bookingList.size() + " bookings");
                            }
                        } else {
                            ToastUtil.showWarning(AdminDashboardActivity.this, "No booking data received");
                            bookingList.clear();
                            bookingAdapter.notifyDataSetChanged();
                            emptyBookingsView.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        }
                    } else {
                        ToastUtil.showError(AdminDashboardActivity.this, "Server returned unsuccessful response");
                        emptyBookingsView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                } else {
                    String errorMsg = "Failed to load bookings";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg = "Error: " + response.code() + " - " + response.message();
                        } catch (Exception e) {
                            errorMsg = "Error code: " + response.code();
                        }
                    }
                    ToastUtil.showError(AdminDashboardActivity.this, errorMsg);
                }
            }

            @Override
            public void onFailure(Call<AdminBookingsResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                String errorMsg = "Network error: " + (t.getMessage() != null ? t.getMessage() : "Unknown error");
                ToastUtil.showError(AdminDashboardActivity.this, errorMsg);
                emptyBookingsView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                t.printStackTrace();
            }
        });
    }

    private void showDatePickerForStart() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            startDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            startDateBtn.setText("From: " + startDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void showDatePickerForEnd() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            endDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            endDateBtn.setText("To: " + endDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void filterBookings() {
        if (startDate.isEmpty() || endDate.isEmpty()) {
            ToastUtil.showWarning(this, "Please select both dates");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        apiService.getBookingsByDateRange(startDate, endDate, "Bearer " + token)
                .enqueue(new Callback<AdminBookingsResponse>() {
                    @Override
                    public void onResponse(Call<AdminBookingsResponse> call, Response<AdminBookingsResponse> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null && response.body().success) {
                            bookingList.clear();
                            bookingList.addAll(response.body().data);
                            bookingAdapter.notifyDataSetChanged();
                            ToastUtil.showSuccess(AdminDashboardActivity.this, "Filtered: " + response.body().data.size() + " bookings");
                        } else {
                            ToastUtil.showInfo(AdminDashboardActivity.this, "No bookings found");
                        }
                    }

                    @Override
                    public void onFailure(Call<AdminBookingsResponse> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        ToastUtil.showError(AdminDashboardActivity.this, "Error: " + t.getMessage());
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
         apiService.cancelBooking(booking.id, "Bearer " + token).enqueue(new Callback<CancelBookingResponse>() {
             @Override
             public void onResponse(Call<CancelBookingResponse> call, Response<CancelBookingResponse> response) {
                 progressBar.setVisibility(View.GONE);
                 if (response.isSuccessful() && response.body() != null && response.body().success) {
                     ToastUtil.showSuccess(AdminDashboardActivity.this, "Booking Rejected/Cancelled");
                     loadAllBookings(); // Reload list
                     loadStatistics(); // Reload stats
                 } else {
                     ToastUtil.showError(AdminDashboardActivity.this, "Failed to reject booking");
                 }
             }

             @Override
             public void onFailure(Call<CancelBookingResponse> call, Throwable t) {
                 progressBar.setVisibility(View.GONE);
                 ToastUtil.showError(AdminDashboardActivity.this, "Error: " + t.getMessage());
             }
         });
    }
    
    @Override
    public void onEdit(Ground ground) {
        Intent intent = new Intent(this, AdminEditGroundActivity.class);
        intent.putExtra("ground", ground);
        startActivity(intent);
    }

    @Override
    public void onDelete(Ground ground) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Ground")
                .setMessage("Delete \"" + ground.name + "\" permanently?")
                .setPositiveButton("Delete", (dialog, which) -> deleteGround(ground))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteGround(Ground ground) {
        progressBar.setVisibility(View.VISIBLE);
        apiService.deleteGround(ground.id, "Bearer " + token).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    ToastUtil.showSuccess(AdminDashboardActivity.this, "Ground deleted");
                    loadAllGrounds();
                } else {
                    ToastUtil.showError(AdminDashboardActivity.this, "Failed to delete ground");
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                ToastUtil.showError(AdminDashboardActivity.this, "Error: " + t.getMessage());
            }
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (isShowingBookings) {
            loadAllBookings();
        } else {
            loadAllGrounds();
        }
        loadStatistics();
    }
}
