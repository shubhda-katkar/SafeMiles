package com.example.safemiles;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminDashboard extends AppCompatActivity {

    FirebaseFirestore firestore;
    LinearLayout blackspotListLayout;
    Button addBlackspotBtn;

    String adminEmail, adminPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        firestore = FirebaseFirestore.getInstance();

        // ✅ Load admin credentials stored during admin login
        SharedPreferences prefs = getSharedPreferences("ADMIN", MODE_PRIVATE);
        adminEmail = prefs.getString("email", null);
        adminPass = prefs.getString("password", null);

        if (adminEmail == null || adminPass == null) {
            Toast.makeText(this, "Admin not logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        addBlackspotBtn = findViewById(R.id.addBlackspotBtn);
        blackspotListLayout = findViewById(R.id.blackspotListLayout);

        addBlackspotBtn.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboard.this, AddBlackspotsActivity.class));
        });

        loadBlackspots();
    }

    private void loadBlackspots() {
        firestore.collection("blackspots")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    blackspotListLayout.removeAllViews();

                    for (DocumentSnapshot doc : value.getDocuments()) {
                        addBlackspotItem(doc.getId(), doc.getString("name"));
                    }
                });
    }

    private void addBlackspotItem(String id, String name) {

        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setPadding(20, 20, 20, 20);
        item.setBackgroundColor(0x33FFFFFF);

        TextView nameView = new TextView(this);
        nameView.setText(name);
        nameView.setTextSize(18);
        nameView.setTextColor(0xFFFFFFFF);
        nameView.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        Button editBtn = new Button(this);
        editBtn.setText("Edit");
        editBtn.setBackgroundTintList(getColorStateList(android.R.color.holo_blue_light));
        editBtn.setOnClickListener(v -> {
            Intent i = new Intent(AdminDashboard.this, EditBlackspotsActivity.class);
            i.putExtra("docId", id);
            startActivity(i);
        });

        Button deleteBtn = new Button(this);
        deleteBtn.setText("Delete");
        deleteBtn.setBackgroundTintList(getColorStateList(android.R.color.holo_red_light));
        deleteBtn.setOnClickListener(v -> deleteBlackspot(id, item));

        item.addView(nameView);
        item.addView(editBtn);
        item.addView(deleteBtn);

        blackspotListLayout.addView(item);
    }

    private Map<String, Object> getAdminAuthMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("adminEmail", adminEmail);
        map.put("adminPass", adminPass);
        return map;
    }

    private void deleteBlackspot(String id, LinearLayout itemView) {

        // Remove item immediately from UI
        blackspotListLayout.removeView(itemView);

        // Temporary admin auth
        Map<String, Object> auth = new HashMap<>();
        auth.put("adminEmail", adminEmail);
        auth.put("adminPass", adminPass);

        // Step 1: authorize admin for delete
        firestore.collection("blackspots").document(id)
                .update(auth)
                .addOnSuccessListener(aVoid -> {

                    // Step 2: now delete
                    firestore.collection("blackspots").document(id)
                            .delete()
                            .addOnSuccessListener(done ->
                                    Toast.makeText(this, "Deleted Successfully", Toast.LENGTH_SHORT).show()
                            )
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                blackspotListLayout.addView(itemView);
                            });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Admin authentication failed!", Toast.LENGTH_SHORT).show();
                    blackspotListLayout.addView(itemView);
                });
    }

}