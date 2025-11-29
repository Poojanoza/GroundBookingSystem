package com.example.groundbookingsystem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.example.groundbookingsystem.adapters.GroundAdapter;
import com.example.groundbookingsystem.api.ApiClient;
import com.example.groundbookingsystem.api.ApiService;
import com.example.groundbookingsystem.models.Ground;
import com.example.groundbookingsystem.models.GroundsResponse;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private GroundAdapter adapter;
    private List<Ground> groundList = new ArrayList<>();
    private ApiService apiService;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Available Grounds");
        }

        recyclerView = findViewById(R.id.groundsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GroundAdapter(this, groundList);
        recyclerView.setAdapter(adapter);

        apiService = ApiClient.getClient().create(ApiService.class);
        loadGrounds();
    }

    private void loadGrounds() {
        if (progressBar != null) {
            progressBar.setVisibility(android.view.View.VISIBLE);
        }
        
        apiService.getGrounds().enqueue(new Callback<GroundsResponse>() {
            @Override
            public void onResponse(Call<GroundsResponse> call, Response<GroundsResponse> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(android.view.View.GONE);
                }
                
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    groundList.clear();
                    groundList.addAll(response.body().data);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to load grounds", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GroundsResponse> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(android.view.View.GONE);
                }
                Toast.makeText(MainActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_bookings) {
            startActivity(new Intent(this, MyBookingsActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_history) {
            startActivity(new Intent(this, BookingHistoryActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        prefs.edit().clear().apply();
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
