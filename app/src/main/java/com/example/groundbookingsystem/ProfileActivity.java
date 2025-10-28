package com.example.groundbookingsystem;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.groundbookingsystem.models.User;

public class ProfileActivity extends AppCompatActivity {
    private TextView nameView, emailView, phoneView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        nameView = findViewById(R.id.profileName);
        emailView = findViewById(R.id.profileEmail);
        phoneView = findViewById(R.id.profilePhone);

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        nameView.setText(prefs.getString("userName", "Name"));
        emailView.setText(prefs.getString("userEmail", "Email"));
        phoneView.setText(prefs.getString("userPhone", "Phone"));
    }
}
