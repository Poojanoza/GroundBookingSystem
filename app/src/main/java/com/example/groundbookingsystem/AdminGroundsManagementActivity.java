package com.example.groundbookingsystem;

import android.content.Intent;
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

import com.example.groundbookingsystem.adapters.AdminGroundAdapter;
import com.example.groundbookingsystem.api.ApiClient;
import com.example.groundbookingsystem.api.ApiService;
import com.example.groundbookingsystem.models.Ground;
import com.example.groundbookingsystem.models.GroundsResponse;
import com.example.groundbookingsystem.models.MessageResponse;
import com.example.groundbookingsystem.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminGroundsManagementActivity extends AppCompatActivity implements AdminGroundAdapter.OnGroundActionListener {

    private RecyclerView recyclerView;
    private AdminGroundAdapter adapter;
    private List<Ground> groundList = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView emptyTextView;
    private ApiService apiService;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_grounds_management);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Grounds");
        }

        initViews();

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        token = prefs.getString("token", "");

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminGroundAdapter(this, groundList, this);
        recyclerView.setAdapter(adapter);

        apiService = ApiClient.getClient().create(ApiService.class);
        loadGrounds();
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
        recyclerView = findViewById(R.id.groundsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyTextView = findViewById(R.id.emptyTextView);
    }

    private void loadGrounds() {
        progressBar.setVisibility(View.VISIBLE);

        apiService.getAdminGrounds("Bearer " + token).enqueue(new Callback<GroundsResponse>() {
            @Override
            public void onResponse(Call<GroundsResponse> call, Response<GroundsResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    groundList.clear();
                    groundList.addAll(response.body().data);
                    adapter.notifyDataSetChanged();

                    if (groundList.isEmpty()) {
                        emptyTextView.setVisibility(View.VISIBLE);
                    } else {
                        emptyTextView.setVisibility(View.GONE);
                    }
                } else {
                    ToastUtil.showError(AdminGroundsManagementActivity.this, "Failed to load grounds");
                }
            }

            @Override
            public void onFailure(Call<GroundsResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                ToastUtil.showError(AdminGroundsManagementActivity.this, "Error: " + t.getMessage());
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
        new AlertDialog.Builder(this)
                .setTitle("Delete Ground")
                .setMessage("Are you sure you want to delete \"" + ground.name + "\"? This action cannot be undone.")
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
                    ToastUtil.showSuccess(AdminGroundsManagementActivity.this, "Ground deleted successfully");
                    loadGrounds(); // Reload list
                } else {
                    ToastUtil.showError(AdminGroundsManagementActivity.this, "Failed to delete ground");
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                ToastUtil.showError(AdminGroundsManagementActivity.this, "Error: " + t.getMessage());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGrounds(); // Refresh when returning from edit activity
    }
}

