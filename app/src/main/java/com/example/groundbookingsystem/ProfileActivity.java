package com.example.groundbookingsystem;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.groundbookingsystem.api.ApiClient;
import com.example.groundbookingsystem.api.ApiService;
import com.example.groundbookingsystem.models.ChangePasswordRequest;
import com.example.groundbookingsystem.models.ProfileUpdateRequest;
import com.example.groundbookingsystem.models.UserProfileResponse;
import com.example.groundbookingsystem.models.MessageResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private TextView nameView, emailView, phoneView;
    private EditText nameEdit, phoneEdit;
    private Button editBtn, changePassBtn;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize views
        nameView = findViewById(R.id.profileName);
        emailView = findViewById(R.id.profileEmail);
        phoneView = findViewById(R.id.profilePhone);
        nameEdit = findViewById(R.id.profileNameEdit);
        phoneEdit = findViewById(R.id.profilePhoneEdit);
        editBtn = findViewById(R.id.editProfileBtn);
        changePassBtn = findViewById(R.id.changePasswordBtn);
        progressBar = findViewById(R.id.progressBar);

        // Get preferences
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String token = prefs.getString("token", "");
        String userId = prefs.getString("userId", "");

        // Set display values
        nameView.setText(prefs.getString("userName", "Name"));
        emailView.setText(prefs.getString("userEmail", "Email"));
        phoneView.setText(prefs.getString("userPhone", "Phone"));
        nameEdit.setText(prefs.getString("userName", ""));
        phoneEdit.setText(prefs.getString("userPhone", ""));

        // API service
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        // Edit profile button listener
        editBtn.setOnClickListener(v -> {
            String newName = nameEdit.getText().toString().trim();
            String newPhone = phoneEdit.getText().toString().trim();

            if (newName.isEmpty() || newPhone.isEmpty()) {
                Toast.makeText(ProfileActivity.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(android.view.View.VISIBLE);
            editBtn.setEnabled(false);

            ProfileUpdateRequest request = new ProfileUpdateRequest(newName, newPhone, null);

            apiService.updateProfile("Bearer " + token, request)
                    .enqueue(new Callback<UserProfileResponse>() {
                        @Override
                        public void onResponse(Call<UserProfileResponse> call,
                                               Response<UserProfileResponse> response) {
                            progressBar.setVisibility(android.view.View.GONE);
                            editBtn.setEnabled(true);

                            if (response.isSuccessful() && response.body() != null) {
                                prefs.edit()
                                        .putString("userName", newName)
                                        .putString("userPhone", newPhone)
                                        .apply();

                                nameView.setText("Name: " + newName);
                                phoneView.setText("Phone: " + newPhone);

                                Toast.makeText(ProfileActivity.this,
                                        "Profile updated successfully",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ProfileActivity.this,
                                        "Failed to update profile",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                            progressBar.setVisibility(android.view.View.GONE);
                            editBtn.setEnabled(true);
                            Toast.makeText(ProfileActivity.this,
                                    "Error: " + t.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Change password button listener
        changePassBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
            LinearLayout layout = new LinearLayout(ProfileActivity.this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(30, 20, 30, 20);

            EditText oldPassEdit = new EditText(ProfileActivity.this);
            oldPassEdit.setHint("Old Password");
            oldPassEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            layout.addView(oldPassEdit);

            EditText newPassEdit = new EditText(ProfileActivity.this);
            newPassEdit.setHint("New Password");
            newPassEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            layout.addView(newPassEdit);

            builder.setView(layout);

            builder.setPositiveButton("Change", (dialog, which) -> {
                String oldPass = oldPassEdit.getText().toString();
                String newPass = newPassEdit.getText().toString();

                if (oldPass.isEmpty() || newPass.isEmpty()) {
                    Toast.makeText(ProfileActivity.this,
                            "Fields cannot be empty",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(android.view.View.VISIBLE);

                ChangePasswordRequest passRequest = new ChangePasswordRequest(oldPass, newPass);

                apiService.changePassword("Bearer " + token, passRequest)
                        .enqueue(new Callback<MessageResponse>() {
                            @Override
                            public void onResponse(Call<MessageResponse> call,
                                                   Response<MessageResponse> response) {
                                progressBar.setVisibility(android.view.View.GONE);

                                if (response.isSuccessful() && response.body() != null) {
                                    Toast.makeText(ProfileActivity.this,
                                            "Password changed successfully",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ProfileActivity.this,
                                            "Failed to change password",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<MessageResponse> call, Throwable t) {
                                progressBar.setVisibility(android.view.View.GONE);
                                Toast.makeText(ProfileActivity.this,
                                        "Error: " + t.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            });

            builder.setNegativeButton("Cancel", null);
            builder.show();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
