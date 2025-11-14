package com.example.safemiles;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class login2 extends AppCompatActivity {

    EditText emailInput, passwordInput;
    Button loginBtn;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginBtn);

        db = FirebaseFirestore.getInstance();

        loginBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter email & password", Toast.LENGTH_SHORT).show();
                return;
            }

            loginAdmin(email, password);
        });
    }

    private void loginAdmin(String email, String password) {
        db.collection("admins").document("admin1")
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Admin account not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String savedEmail = doc.getString("email");
                    String savedPass = doc.getString("password");

                    if (email.equals(savedEmail) && password.equals(savedPass)) {

                        Toast.makeText(this, "Admin Login Successful", Toast.LENGTH_SHORT).show();

                        // ✅ Save admin credentials for Firestore write permission
                        SharedPreferences prefs = getSharedPreferences("ADMIN", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("email", email);
                        editor.putString("password", password);
                        editor.apply();

                        // ✅ Redirect to Admin Dashboard
                        Intent intent = new Intent(login2.this, AdminDashboard.class);
                        startActivity(intent);
                        finish();

                    } else {
                        Toast.makeText(this, "Incorrect credentials", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
