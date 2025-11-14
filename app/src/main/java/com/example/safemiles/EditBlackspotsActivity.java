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

public class EditBlackspotsActivity extends AppCompatActivity {

    EditText nameInput, reasonInput, latitudeInput, longitudeInput, radiusInput;
    Button updateBtn;

    FirebaseFirestore firestore;
    String docId;

    String adminEmail, adminPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_blackspots);

        firestore = FirebaseFirestore.getInstance();

        // ✅ Load admin credentials
        SharedPreferences prefs = getSharedPreferences("ADMIN", MODE_PRIVATE);
        adminEmail = prefs.getString("email", null);
        adminPass = prefs.getString("password", null);

        if (adminEmail == null || adminPass == null) {
            Toast.makeText(this, "Admin not logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        docId = getIntent().getStringExtra("docId");

        nameInput = findViewById(R.id.editNameInput);
        reasonInput = findViewById(R.id.editReasonInput);
        latitudeInput = findViewById(R.id.editLatitudeInput);
        longitudeInput = findViewById(R.id.editLongitudeInput);
        radiusInput = findViewById(R.id.editRadiusInput);
        updateBtn = findViewById(R.id.updateBtn);

        loadBlackspot();

        updateBtn.setOnClickListener(v -> updateBlackspot());
    }

    private void loadBlackspot() {
        firestore.collection("blackspots").document(docId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {

                        nameInput.setText(doc.getString("name"));
                        reasonInput.setText(doc.getString("reason"));

                        Double lat = doc.getDouble("latitude");
                        Double lng = doc.getDouble("longitude");
                        Long radius = doc.getLong("radius");

                        latitudeInput.setText(lat != null ? String.valueOf(lat) : "");
                        longitudeInput.setText(lng != null ? String.valueOf(lng) : "");
                        radiusInput.setText(radius != null ? String.valueOf(radius) : "");
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void updateBlackspot() {

        String name = nameInput.getText().toString().trim();
        String reason = reasonInput.getText().toString().trim();
        String latStr = latitudeInput.getText().toString().trim();
        String lngStr = longitudeInput.getText().toString().trim();
        String radiusStr = radiusInput.getText().toString().trim();

        if (name.isEmpty() || reason.isEmpty() || latStr.isEmpty()
                || lngStr.isEmpty() || radiusStr.isEmpty()) {
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

        // ✅ Firestore requires admin authentication fields
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("name", name);
        updatedData.put("reason", reason);
        updatedData.put("latitude", lat);
        updatedData.put("longitude", lng);
        updatedData.put("radius", radius);
        updatedData.put("adminEmail", adminEmail);
        updatedData.put("adminPass", adminPass);

        firestore.collection("blackspots").document(docId)
                .set(updatedData)
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "✅ Updated Successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "❌ Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
