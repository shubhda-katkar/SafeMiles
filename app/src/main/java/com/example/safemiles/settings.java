package com.example.safemiles;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;

public class settings extends AppCompatActivity {

    private Switch locationSwitch, notificationSwitch;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        // Initialize UI components
        locationSwitch = findViewById(R.id.locationSwitch);
        notificationSwitch = findViewById(R.id.notificationSwitch);
        logoutButton = findViewById(R.id.logoutButton);

        // Load switch states based on phone settings
        checkPermissions();

        // ✅ Location toggle
        locationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                requestLocationPermission();
            } else {
                Toast.makeText(this, "Location access disabled", Toast.LENGTH_SHORT).show();
            }
        });

        // ✅ Notification toggle
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                openNotificationSettings();
            } else {
                Toast.makeText(this, "Notification access disabled", Toast.LENGTH_SHORT).show();
            }
        });

        // ✅ Logout
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, login.class));
            finish();
        });
    }

    // ✅ Check and update switch states
    private void checkPermissions() {
        boolean locationGranted =
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;

        locationSwitch.setChecked(locationGranted);

        // Notification permission — Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean notifGranted =
                    ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                            == PackageManager.PERMISSION_GRANTED;
            notificationSwitch.setChecked(notifGranted);
        } else {
            notificationSwitch.setChecked(true);
        }
    }

    // ✅ Request location permission
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                100
        );
    }

    // ✅ Go to system notification settings
    private void openNotificationSettings() {
        Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        startActivity(intent);
    }
}
