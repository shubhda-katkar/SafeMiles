package com.example.safemiles;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (auth.getCurrentUser() != null) {
                    // ✅ User already logged in → go to home
                    startActivity(new Intent(MainActivity.this, home.class));
                } else {
                    // ❌ Not logged in → go to login
                    startActivity(new Intent(MainActivity.this, login.class));
                }
                finish();
            }
        }, 3000);
    }
}
