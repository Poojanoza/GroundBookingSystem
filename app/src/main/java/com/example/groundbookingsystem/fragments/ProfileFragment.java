package com.example.groundbookingsystem.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.groundbookingsystem.LoginActivity;
import com.example.groundbookingsystem.R;
import com.example.groundbookingsystem.api.ApiClient;
import com.example.groundbookingsystem.api.ApiService;
import com.example.groundbookingsystem.models.ChangePasswordRequest;
import com.example.groundbookingsystem.models.MessageResponse;
import com.example.groundbookingsystem.models.ProfileUpdateRequest;
import com.example.groundbookingsystem.models.UserProfileResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.view.ViewGroup;
import android.widget.ImageView;

public class ProfileFragment extends Fragment {

    private TextView nameView, emailView, phoneView;
    private EditText nameEdit, phoneEdit;
    private Button editBtn, changePassBtn, logoutBtn;
    private ImageView editNameIcon, editPhoneIcon, closeEditIcon;
    private ViewGroup editProfileCard;
    private ProgressBar progressBar;
    private String token;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize views
        nameView = view.findViewById(R.id.profileName);
        emailView = view.findViewById(R.id.profileEmail);
        phoneView = view.findViewById(R.id.profilePhone);
        nameEdit = view.findViewById(R.id.profileNameEdit);
        phoneEdit = view.findViewById(R.id.profilePhoneEdit);
        editBtn = view.findViewById(R.id.editProfileBtn);
        changePassBtn = view.findViewById(R.id.changePasswordBtn);
        logoutBtn = view.findViewById(R.id.logoutBtn);
        progressBar = view.findViewById(R.id.progressBar);
        editNameIcon = view.findViewById(R.id.editNameIcon);
        editPhoneIcon = view.findViewById(R.id.editPhoneIcon);
        closeEditIcon = view.findViewById(R.id.closeEditIcon);
        editProfileCard = view.findViewById(R.id.editProfileCard);

        if (getContext() != null) {
            // Get preferences
            SharedPreferences prefs = getContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
            token = prefs.getString("token", "");

            // Load user profile from API
            apiService = ApiClient.getClient().create(ApiService.class);
            loadUserProfile();
        }

        // Edit icon listeners
        if (editNameIcon != null) {
            editNameIcon.setOnClickListener(v -> showEditDialog("name"));
        }
        
        if (editPhoneIcon != null) {
            editPhoneIcon.setOnClickListener(v -> showEditDialog("phone"));
        }
        
        if (closeEditIcon != null) {
            closeEditIcon.setOnClickListener(v -> hideEditDialog());
        }

        // Edit profile button listener
        editBtn.setOnClickListener(v -> {
            String newName = nameEdit.getText().toString().trim();
            String newPhone = phoneEdit.getText().toString().trim();

            if (newName.isEmpty() || newPhone.isEmpty()) {
                Toast.makeText(getContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            editBtn.setEnabled(false);

            ProfileUpdateRequest request = new ProfileUpdateRequest(newName, newPhone, null);

            apiService.updateProfile("Bearer " + token, request)
                    .enqueue(new Callback<UserProfileResponse>() {
                        @Override
                        public void onResponse(Call<UserProfileResponse> call,
                                               Response<UserProfileResponse> response) {
                            progressBar.setVisibility(View.GONE);
                            editBtn.setEnabled(true);

                            if (response.isSuccessful() && response.body() != null && getContext() != null) {
                                SharedPreferences prefs = getContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
                                prefs.edit()
                                        .putString("userName", newName)
                                        .putString("userPhone", newPhone)
                                        .apply();

                                nameView.setText(newName);
                                phoneView.setText(newPhone);
                                hideEditDialog();

                                Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                            progressBar.setVisibility(View.GONE);
                            editBtn.setEnabled(true);
                            if (getContext() != null)
                                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Change password button listener
        changePassBtn.setOnClickListener(v -> {
            if (getContext() == null) return;
            
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            LinearLayout layout = new LinearLayout(getContext());
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(30, 20, 30, 20);

            EditText oldPassEdit = new EditText(getContext());
            oldPassEdit.setHint("Old Password");
            oldPassEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            layout.addView(oldPassEdit);

            EditText newPassEdit = new EditText(getContext());
            newPassEdit.setHint("New Password");
            newPassEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            layout.addView(newPassEdit);

            builder.setView(layout);

            builder.setPositiveButton("Change", (dialog, which) -> {
                String oldPass = oldPassEdit.getText().toString();
                String newPass = newPassEdit.getText().toString();

                if (oldPass.isEmpty() || newPass.isEmpty()) {
                    Toast.makeText(getContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                ChangePasswordRequest passRequest = new ChangePasswordRequest(oldPass, newPass);

                apiService.changePassword("Bearer " + token, passRequest)
                        .enqueue(new Callback<MessageResponse>() {
                            @Override
                            public void onResponse(Call<MessageResponse> call,
                                                   Response<MessageResponse> response) {
                                progressBar.setVisibility(View.GONE);

                                if (response.isSuccessful() && response.body() != null && getContext() != null) {
                                    Toast.makeText(getContext(), "Password changed successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Failed to change password", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<MessageResponse> call, Throwable t) {
                                progressBar.setVisibility(View.GONE);
                                if (getContext() != null)
                                    Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            });

            builder.setNegativeButton("Cancel", null);
            builder.show();
        });

        logoutBtn.setOnClickListener(v -> {
            if (getContext() != null) {
                SharedPreferences prefs = getContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
                prefs.edit().clear().apply();
                Toast.makeText(getContext(), "Logged out", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getContext(), LoginActivity.class));
                if (getActivity() != null) getActivity().finish();
            }
        });

        return view;
    }
    
    private void loadUserProfile() {
        if (getContext() == null || apiService == null) return;
        
        progressBar.setVisibility(View.VISIBLE);
        
        apiService.getUserProfile("Bearer " + token).enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().success && getContext() != null) {
                    UserProfileResponse.UserProfile user = response.body().data;
                    if (user != null) {
                        // Update UI with user data
                        nameView.setText(user.name != null ? user.name : "Name");
                        emailView.setText(user.email != null ? user.email : "Email");
                        phoneView.setText(user.phone != null ? user.phone : "Phone");
                        
                        // Update preferences
                        SharedPreferences prefs = getContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
                        prefs.edit()
                            .putString("userName", user.name != null ? user.name : "")
                            .putString("userEmail", user.email != null ? user.email : "")
                            .putString("userPhone", user.phone != null ? user.phone : "")
                            .apply();
                            
                        // Set edit fields
                        nameEdit.setText(user.name != null ? user.name : "");
                        phoneEdit.setText(user.phone != null ? user.phone : "");
                    }
                } else {
                    // Fallback to preferences if API fails
                    SharedPreferences prefs = getContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
                    nameView.setText(prefs.getString("userName", "Name"));
                    emailView.setText(prefs.getString("userEmail", "Email"));
                    phoneView.setText(prefs.getString("userPhone", "Phone"));
                    nameEdit.setText(prefs.getString("userName", ""));
                    phoneEdit.setText(prefs.getString("userPhone", ""));
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                // Fallback to preferences
                if (getContext() != null) {
                    SharedPreferences prefs = getContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
                    nameView.setText(prefs.getString("userName", "Name"));
                    emailView.setText(prefs.getString("userEmail", "Email"));
                    phoneView.setText(prefs.getString("userPhone", "Phone"));
                    nameEdit.setText(prefs.getString("userName", ""));
                    phoneEdit.setText(prefs.getString("userPhone", ""));
                }
            }
        });
    }
    
    private void showEditDialog(String field) {
        if (editProfileCard != null) {
            editProfileCard.setVisibility(View.VISIBLE);
            if (field.equals("name")) {
                nameEdit.requestFocus();
            } else if (field.equals("phone")) {
                phoneEdit.requestFocus();
            }
        }
    }
    
    private void hideEditDialog() {
        if (editProfileCard != null) {
            editProfileCard.setVisibility(View.GONE);
        }
    }
}
