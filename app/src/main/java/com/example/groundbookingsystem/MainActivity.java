package com.example.groundbookingsystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.groundbookingsystem.fragments.HomeFragment;
import com.example.groundbookingsystem.fragments.MyBookingsFragment;
import com.example.groundbookingsystem.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Ground Booking System");
        }

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            try {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();
                
                if (itemId == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                    if (getSupportActionBar() != null) getSupportActionBar().setTitle("Available Grounds");
                } else if (itemId == R.id.nav_bookings) {
                    selectedFragment = new MyBookingsFragment();
                    if (getSupportActionBar() != null) getSupportActionBar().setTitle("My Bookings");
                } else if (itemId == R.id.nav_profile) {
                    selectedFragment = new ProfileFragment();
                    if (getSupportActionBar() != null) getSupportActionBar().setTitle("My Profile");
                }

                if (selectedFragment != null) {
                    try {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, selectedFragment)
                                .commitAllowingStateLoss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });

        // Load default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }

    // Removed onCreateOptionsMenu since we now have Bottom Navigation
}
