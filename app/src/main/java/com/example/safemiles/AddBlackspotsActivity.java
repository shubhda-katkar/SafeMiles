package com.example.safemiles;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddBlackspotsActivity extends AppCompatActivity {

    EditText nameInput, reasonInput, latitudeInput, longitudeInput, radiusInput;
    Button saveBtn;

    FirebaseFirestore firestore;

    String adminEmail, adminPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_blackspots);

        firestore = FirebaseFirestore.getInstance();

        // ✅ Load admin credentials saved during admin login
        SharedPreferences prefs = getSharedPreferences("ADMIN", MODE_PRIVATE);
        adminEmail = prefs.getString("email", null);
        adminPass = prefs.getString("password", null);

        if (adminEmail == null || adminPass == null) {
            Toast.makeText(this, "Admin not logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        nameInput = findViewById(R.id.nameInput);
        reasonInput = findViewById(R.id.reasonInput);
        latitudeInput = findViewById(R.id.latitudeInput);
        longitudeInput = findViewById(R.id.longitudeInput);
        radiusInput = findViewById(R.id.radiusInput);
        saveBtn = findViewById(R.id.saveBtn);

        saveBtn.setOnClickListener(v -> saveBlackspot());
    }

    private void saveBlackspot() {
        String name = nameInput.getText().toString().trim();
        String reason = reasonInput.getText().toString().trim();
        String latStr = latitudeInput.getText().toString().trim();
        String lngStr = longitudeInput.getText().toString().trim();
        String radiusStr = radiusInput.getText().toString().trim();

        // ✅ Validation
        if (name.isEmpty() || reason.isEmpty() || latStr.isEmpty() ||
                lngStr.isEmpty() || radiusStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double lat, lng;
        int radius;

        try {
            lat = Double.parseDouble(latStr);
            lng = Double.parseDouble(lngStr);
            radius = Integer.parseInt(radiusStr);
        } catch (Exception e) {
            Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Firestore fields including authentication
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("reason", reason);
        data.put("latitude", lat);
        data.put("longitude", lng);
        data.put("radius", radius);

        // ✅ Required for Firestore write rules
        data.put("adminEmail", adminEmail);
        data.put("adminPass", adminPass);

        // ✅ Save blackspot
        firestore.collection("blackspots")
                .add(data)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this, "✅ Blackspot Added!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "❌ Failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
