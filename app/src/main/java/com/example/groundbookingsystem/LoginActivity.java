package com.example.groundbookingsystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.groundbookingsystem.api.ApiClient;
import com.example.groundbookingsystem.api.ApiService;
import com.example.groundbookingsystem.models.AuthResponse;
import com.example.groundbookingsystem.models.LoginRequest;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView registerTextView;
    private ProgressBar progressBar;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerTextView = findViewById(R.id.registerTextView);
        progressBar = findViewById(R.id.progressBar);

        apiService = ApiClient.getClient().create(ApiService.class);

        loginButton.setOnClickListener(view -> loginUser());

        registerTextView.setOnClickListener(view ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);

        LoginRequest request = new LoginRequest(email, password);
        Call<AuthResponse> call = apiService.login(request);

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                progressBar.setVisibility(View.GONE);
                loginButton.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                    prefs.edit()
                            .putString("token", response.body().token)
                            .putString("userId", response.body().user.id)
                            .putString("userName", response.body().user.name)
                            .putBoolean("isAdmin", response.body().user.is_admin)
                            .apply();
                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                    
                    // Route to appropriate screen based on role
                    if (response.body().user.is_admin) {
                        startActivity(new Intent(LoginActivity.this, AdminDashboardActivity.class));
                    } else {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    }
                    finish();
                } else {
                    String errorMessage = "Login failed";
                    if (response.errorBody() != null) {
                        try {
                            AuthResponse errorResponse = new Gson().fromJson(response.errorBody().charStream(), AuthResponse.class);
                            if (errorResponse != null && errorResponse.message != null) {
                                errorMessage = errorResponse.message;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (response.body() != null && response.body().message != null) {
                         errorMessage = response.body().message;
                    }
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                loginButton.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
