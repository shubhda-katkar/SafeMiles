package com.example.safemiles;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class home extends AppCompatActivity {

    private static final int REQUEST_CHECK_SETTINGS = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        TextView mapBtn = findViewById(R.id.mapEmoji);
        TextView dial = findViewById(R.id.dialEmoji);
        TextView settings= findViewById(R.id.settingsEmoji);
        TextView guidelines = findViewById(R.id.guide);

        guidelines.setText(Html.fromHtml("<a href='https://highwaypolice.maharashtra.gov.in/info-rules-page/'>Safe Driving Guidelines</a>"));
        guidelines.setMovementMethod(LinkMovementMethod.getInstance());

        dial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(home.this,dial.class);
                startActivity(intent);
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(home.this,settings.class);
                startActivity(intent);
            }
        });

        // Check location before going to map
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLocationSettings();
            }
        });
    }

    private void checkLocationSettings() {
        // Create a high-accuracy location request
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task =
                settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // ✅ Location is ON → open map
                startActivity(new Intent(home.this, MapActivity.class));
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        // ⚠️ Ask user to enable location
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(home.this, REQUEST_CHECK_SETTINGS);
                    } catch (Exception sendEx) {
                        Toast.makeText(home.this, "Unable to request location enable.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(home.this, "Location services not available.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Handle result from location enable dialog
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                // ✅ User enabled location → go to MapActivity
                startActivity(new Intent(home.this, MapActivity.class));
            } else {
                // ❌ User refused
                Toast.makeText(this, "Location is required to view the map.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}