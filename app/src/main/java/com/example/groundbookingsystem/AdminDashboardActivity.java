package com.example.groundbookingsystem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.groundbookingsystem.api.ApiClient;
import com.example.groundbookingsystem.api.ApiService;
import com.example.groundbookingsystem.models.AdminStatsResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardActivity extends AppCompatActivity {

    private ApiService apiService;
    private String token;
    private TextView totalBookingsView, totalRevenueView, activeBookingsView, cancelledBookingsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        initViews();
        setupToolbar();

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        token = prefs.getString("token", "");

        apiService = ApiClient.getClient().create(ApiService.class);
        
        // FAB for creating ground
        View fab = findViewById(R.id.addGroundFab);
        if (fab != null) {
            fab.setOnClickListener(v -> {
                startActivity(new Intent(AdminDashboardActivity.this, AdminCreateGroundActivity.class));
            });
        }

        loadStatistics();
    }

    private void initViews() {
        totalBookingsView = findViewById(R.id.totalBookingsView);
        totalRevenueView = findViewById(R.id.totalRevenueView);
        activeBookingsView = findViewById(R.id.activeBookingsView);
        cancelledBookingsView = findViewById(R.id.cancelledBookingsView);
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

    @Override
    protected void onResume() {
        super.onResume();
        loadStatistics();
    }

    // Quick Actions - Handle clicks from layout
    public void onManageGroundsClick(View view) {
        startActivity(new Intent(this, AdminGroundsManagementActivity.class));
    }

    public void onAllBookingsClick(View view) {
        startActivity(new Intent(this, AdminAllBookingsActivity.class));
    }
}
