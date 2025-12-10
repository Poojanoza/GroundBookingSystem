package com.example.groundbookingsystem;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.bumptech.glide.Glide;
import com.example.groundbookingsystem.api.ApiClient;
import com.example.groundbookingsystem.api.ApiService;
import com.example.groundbookingsystem.models.CreateGroundRequest;
import com.example.groundbookingsystem.models.GroundDetailResponse;
import com.example.groundbookingsystem.models.ImageUploadResponse;
import com.example.groundbookingsystem.utils.ToastUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminCreateGroundActivity extends AppCompatActivity {

    private EditText nameEditText, locationEditText, typeEditText, descriptionEditText, priceEditText;
    private Button selectImageBtn, createGroundBtn;
    private ImageView imagePreview;
    private ProgressBar progressBar;
    private ApiService apiService;
    private String token;
    private String uploadedImageUrl = "";
    private Uri selectedImageUri = null;

    // Image Picker Launcher
    private final ActivityResultLauncher<android.content.Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        imagePreview.setImageTintList(null); // Remove placeholder tint
                        imagePreview.setPadding(0,0,0,0); // Remove placeholder padding
                        Glide.with(this).load(selectedImageUri).into(imagePreview);
                        selectImageBtn.setText("Change Image");
                    }
                }
            });

    // Permission Launcher
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openImagePicker();
                } else {
                    ToastUtil.showError(this, "Permission denied");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_create_ground);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        initViews();

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        token = prefs.getString("token", "");

        apiService = ApiClient.getClient().create(ApiService.class);

        selectImageBtn.setOnClickListener(v -> checkPermissionAndPickImage());
        createGroundBtn.setOnClickListener(v -> validateAndCreate());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        nameEditText = findViewById(R.id.nameEditText);
        locationEditText = findViewById(R.id.locationEditText);
        typeEditText = findViewById(R.id.typeEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        priceEditText = findViewById(R.id.priceEditText);
        selectImageBtn = findViewById(R.id.selectImageBtn);
        createGroundBtn = findViewById(R.id.createGroundBtn);
        imagePreview = findViewById(R.id.imagePreview);
        progressBar = findViewById(R.id.progressBar);
    }

    private void checkPermissionAndPickImage() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openImagePicker();
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void validateAndCreate() {
        String name = nameEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();
        String type = typeEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String priceStr = priceEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Ground name is required");
            return;
        }
        if (TextUtils.isEmpty(location)) {
            locationEditText.setError("Location is required");
            return;
        }
        if (TextUtils.isEmpty(type)) {
            typeEditText.setError("Type is required");
            return;
        }
        if (TextUtils.isEmpty(description)) {
            descriptionEditText.setError("Description is required");
            return;
        }
        if (TextUtils.isEmpty(priceStr)) {
            priceEditText.setError("Price is required");
            return;
        }
        if (selectedImageUri == null) {
            ToastUtil.showWarning(this, "Please select an image");
            return;
        }

        double price = Double.parseDouble(priceStr);
        uploadImageAndCreateGround(name, location, type, description, price);
    }

    private void uploadImageAndCreateGround(String name, String location, String type, String description, double price) {
        progressBar.setVisibility(View.VISIBLE);
        createGroundBtn.setEnabled(false);

        File imageFile = createTempFileFromUri(selectedImageUri);
        if (imageFile == null) {
            progressBar.setVisibility(View.GONE);
            createGroundBtn.setEnabled(true);
            ToastUtil.showError(this, "Failed to process image file");
            return;
        }

        String mimeType = getContentResolver().getType(selectedImageUri);
        if (mimeType == null || !mimeType.startsWith("image/")) {
            mimeType = "image/jpeg";
        }

        RequestBody requestBody = RequestBody.create(MediaType.parse(mimeType), imageFile);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("image", imageFile.getName(), requestBody);

        apiService.uploadGroundImage("Bearer " + token, filePart)
                .enqueue(new Callback<ImageUploadResponse>() {
                    @Override
                    public void onResponse(Call<ImageUploadResponse> call, Response<ImageUploadResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().success) {
                            uploadedImageUrl = response.body().url;
                            createGroundWithImage(name, location, type, description, price);
                        } else {
                            progressBar.setVisibility(View.GONE);
                            createGroundBtn.setEnabled(true);
                            ToastUtil.showError(AdminCreateGroundActivity.this, "Image upload failed: " + (response.body() != null ? response.body().message : response.message()));
                        }
                    }

                    @Override
                    public void onFailure(Call<ImageUploadResponse> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        createGroundBtn.setEnabled(true);
                            ToastUtil.showError(AdminCreateGroundActivity.this, "Upload error: " + t.getMessage());
                    }
                });
    }

    private File createTempFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File tempFile = File.createTempFile("upload", ".jpg", getCacheDir());
            OutputStream outputStream = new FileOutputStream(tempFile);
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            outputStream.close();
            inputStream.close();
            return tempFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void createGroundWithImage(String name, String location, String type, String description, double price) {
        CreateGroundRequest request = new CreateGroundRequest(name, location, type, description, price, uploadedImageUrl);

        apiService.createGround("Bearer " + token, request)
                .enqueue(new Callback<GroundDetailResponse>() {
                    @Override
                    public void onResponse(Call<GroundDetailResponse> call, Response<GroundDetailResponse> response) {
                        progressBar.setVisibility(View.GONE);
                        createGroundBtn.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null && response.body().success) {
                            ToastUtil.showSuccess(AdminCreateGroundActivity.this, "Ground created successfully!");
                            finish();
                        } else {
                            ToastUtil.showError(AdminCreateGroundActivity.this, "Failed to create ground");
                        }
                    }

                    @Override
                    public void onFailure(Call<GroundDetailResponse> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        createGroundBtn.setEnabled(true);
                            ToastUtil.showError(AdminCreateGroundActivity.this, "Error: " + t.getMessage());
                    }
                });
    }
}
